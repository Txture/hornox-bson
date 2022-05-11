package io.txture.hornoxbson.test.cases

import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting
import io.txture.hornoxbson.model.Decimal128Node
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class Decimal128NodeIoTest : IoTest() {

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeDecimal128Node(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val bytes = this.serializeSingeNode(Decimal128Node(ByteArray(16)), sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(16)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getLong(0) }.isEqualTo(0)
                get { this.getLong(Long.SIZE_BYTES) }.isEqualTo(0)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeDecimal128Node(trustSizeMarkers: Boolean) {
        val emptyTextNodeBinary = ByteArray(16)
        val buffer = ByteBuffer.wrap(emptyTextNodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putLong(123)
        buffer.putLong(456)
        val node = this.deserializeSingleNode(emptyTextNodeBinary, 0x13)
        expectThat(node).isA<Decimal128Node>().and {
            get { this.value.size }.isEqualTo(16)
            get { ByteBuffer.wrap(this.value).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getLong(0) }.isEqualTo(123)
                get { this.getLong(Long.SIZE_BYTES) }.isEqualTo(456)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverDecimal128Node(trustSizeMarkers: Boolean) {
        assertCanSkipOverNode(Decimal128Node(ByteArray(16)), trustSizeMarkers)
    }

}