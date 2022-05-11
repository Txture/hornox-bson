package io.txture.hornoxbson.test.cases

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting
import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.model.DbPointerNode
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class DbPointerNodeIoTest : IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "300000000c610008000000666f6f62617221000000000000000000000000000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = DbPointerNode("foobar!", ByteArray(12))
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeDbPointerNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val name = "hello"
        val nameUtf8 = name.toByteArray()
        val content = ByteArray(12)
        ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(Int.MIN_VALUE)
            .putInt(0)
            .putInt(Int.MAX_VALUE)
        val bytes = this.serializeSingeNode(DbPointerNode(name = name, value = content), sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(Int.SIZE_BYTES /* string length */ + nameUtf8.size + 1 /* null terminal of the name */ + 12 /* fixed-length byte array */)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(0) }.isEqualTo(6) // h, e, l, l, o, null
                get { this[Int.SIZE_BYTES + 0] }.isEqualTo("h".toByteArray().single())
                get { this[Int.SIZE_BYTES + 1] }.isEqualTo("e".toByteArray().single())
                get { this[Int.SIZE_BYTES + 2] }.isEqualTo("l".toByteArray().single())
                get { this[Int.SIZE_BYTES + 3] }.isEqualTo("l".toByteArray().single())
                get { this[Int.SIZE_BYTES + 4] }.isEqualTo("o".toByteArray().single())
                get { this[Int.SIZE_BYTES + 5] }.isEqualTo(0x00)
                get { this.getInt(Int.SIZE_BYTES + 6 + Int.SIZE_BYTES * 0) }.isEqualTo(Int.MIN_VALUE)
                get { this.getInt(Int.SIZE_BYTES + 6 + Int.SIZE_BYTES * 1) }.isEqualTo(0)
                get { this.getInt(Int.SIZE_BYTES + 6 + Int.SIZE_BYTES * 2) }.isEqualTo(Int.MAX_VALUE)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeDbPointerNode(trustSizeMarkers: Boolean) {
        val name = "hello"
        val nameUtf8 = name.toByteArray()
        val bytes = ByteArray(Int.SIZE_BYTES /* string length */ + nameUtf8.size + 1 /* null terminal of the name */ + 12 /* fixed-length byte array */)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(6)
            .put(nameUtf8)
            .put(0x00)
            .putInt(Int.MAX_VALUE)
            .putInt(0)
            .putInt(Int.MIN_VALUE)
        val node = this.deserializeSingleNode(bytes, 0x0C)
        expectThat(node).isA<DbPointerNode>().and {
            get { this.value.size }.isEqualTo(12)
            get { this.name }.isEqualTo("hello")
            get { ByteBuffer.wrap(this.value).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(Int.SIZE_BYTES * 0) }.isEqualTo(Int.MAX_VALUE)
                get { this.getInt(Int.SIZE_BYTES * 1) }.isEqualTo(0)
                get { this.getInt(Int.SIZE_BYTES * 2) }.isEqualTo(Int.MIN_VALUE)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverDbPointerNode(trustSizeMarkers: Boolean) {
        val content = ByteArray(12)
        ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(Int.MAX_VALUE)
            .putInt(0)
            .putInt(Int.MIN_VALUE)
        assertCanSkipOverNode(DbPointerNode(name = "foobar!", value = content), trustSizeMarkers)
    }

}