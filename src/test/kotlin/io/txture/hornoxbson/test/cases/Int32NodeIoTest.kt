package io.txture.hornoxbson.test.cases

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting
import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.Int32Node
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class Int32NodeIoTest : IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "1c0000001061002a0000000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = Int32Node(42)
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @CsvSource(
        "WRITE_MINUS_1, -2147483648",
        "WRITE_MINUS_1, -3000",
        "WRITE_MINUS_1, -1",
        "WRITE_MINUS_1, 0",
        "WRITE_MINUS_1, 1",
        "WRITE_MINUS_1, 3000",
        "WRITE_MINUS_1, 2147483647",

        "TRUST_DOCUMENT, -2147483648",
        "TRUST_DOCUMENT, -3000",
        "TRUST_DOCUMENT, -1",
        "TRUST_DOCUMENT, 0",
        "TRUST_DOCUMENT, 1",
        "TRUST_DOCUMENT, 3000",
        "TRUST_DOCUMENT, 2147483647",

        "RECOMPUTE, -2147483648",
        "RECOMPUTE, -3000",
        "RECOMPUTE, -1",
        "RECOMPUTE, 0",
        "RECOMPUTE, 1",
        "RECOMPUTE, 3000",
        "RECOMPUTE, 2147483647",
    )
    fun canSerializeInt32Node(
        sizeMarkersWriterSetting: SizeMarkersWriterSetting,
        value: Int
    ) {
        val bytes = this.serializeSingeNode(Int32Node(value), sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(Int.SIZE_BYTES)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(0) }.isEqualTo(value)
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "true, -2147483648",
        "true, -3000",
        "true, -1",
        "true, 0",
        "true, 1",
        "true, 3000",
        "true, 2147483647",

        "false, -2147483648",
        "false, -3000",
        "false, -1",
        "false, 0",
        "false, 1",
        "false, 3000",
        "false, 2147483647",
    )
    fun canDeserializeInt32Node(trustSizeMarkers: Boolean, value: Int) {
        val nodeBinary = ByteArray(Int.SIZE_BYTES)
        val buffer = ByteBuffer.wrap(nodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(value)
        val node = this.deserializeSingleNode(nodeBinary, 0x10)
        expectThat(node).isA<Int32Node>().get { this.value }.isEqualTo(value)
    }

    @ParameterizedTest
    @CsvSource(
        "true, -2147483648",
        "true, -3000",
        "true, -1",
        "true, 0",
        "true, 1",
        "true, 3000",
        "true, 2147483647",

        "false, -2147483648",
        "false, -3000",
        "false, -1",
        "false, 0",
        "false, 1",
        "false, 3000",
        "false, 2147483647",
    )
    fun canSkipOverUtcDateTimeNode(trustSizeMarkers: Boolean, value: Int) {
        assertCanSkipOverNode(Int32Node(value), trustSizeMarkers)
    }

}