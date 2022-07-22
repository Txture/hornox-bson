package io.txture.hornoxbson.test.cases

import io.txture.hornoxbson.BsonDeserializer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting
import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.Int32Node
import io.txture.hornoxbson.model.Int64Node
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class Int64NodeIoTest : IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "200000001261009975a4b4800100000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = Int64Node(1652298118553L)
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @CsvSource(
        "WRITE_MINUS_1, -9223372036854775807",
        "WRITE_MINUS_1, -1652211721080",
        "WRITE_MINUS_1, -3000",
        "WRITE_MINUS_1, -1",
        "WRITE_MINUS_1, 0",
        "WRITE_MINUS_1, 1",
        "WRITE_MINUS_1, 3000",
        "WRITE_MINUS_1, 1652211721080",
        "WRITE_MINUS_1, 9223372036854775807",

        "TRUST_DOCUMENT, -9223372036854775807",
        "TRUST_DOCUMENT, -1652211721080",
        "TRUST_DOCUMENT, -3000",
        "TRUST_DOCUMENT, -1",
        "TRUST_DOCUMENT, 0",
        "TRUST_DOCUMENT, 1",
        "TRUST_DOCUMENT, 3000",
        "TRUST_DOCUMENT, 1652211721080",
        "TRUST_DOCUMENT, 9223372036854775807",

        "RECOMPUTE, -9223372036854775807",
        "RECOMPUTE, -1652211721080",
        "RECOMPUTE, -3000",
        "RECOMPUTE, -1",
        "RECOMPUTE, 0",
        "RECOMPUTE, 1",
        "RECOMPUTE, 3000",
        "RECOMPUTE, 1652211721080",
        "RECOMPUTE, 9223372036854775807",
    )
    fun canSerializeInt64Node(
        sizeMarkersWriterSetting: SizeMarkersWriterSetting,
        value: Long
    ) {
        val bytes = this.serializeSingeNode(Int64Node(value), sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(Long.SIZE_BYTES)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getLong(0) }.isEqualTo(value)
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "true, -9223372036854775807",
        "true, -1652211721080",
        "true, -3000",
        "true, -1",
        "true, 0",
        "true, 1",
        "true, 3000",
        "true, 1652211721080",
        "true, 9223372036854775807",

        "false, -9223372036854775807",
        "false, -1652211721080",
        "false, -3000",
        "false, -1",
        "false, 0",
        "false, 1",
        "false, 3000",
        "false, 1652211721080",
        "false, 9223372036854775807",
    )
    fun canDeserializeInt64Node(trustSizeMarkers: Boolean, value: Long) {
        val nodeBinary = ByteArray(Long.SIZE_BYTES)
        val buffer = ByteBuffer.wrap(nodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putLong(value)
        val node = this.deserializeSingleNode(nodeBinary, 0x12)
        expectThat(node).isA<Int64Node>().get { this.value }.isEqualTo(value)
    }

    @ParameterizedTest
    @CsvSource(
        "true, -9223372036854775807",
        "true, -1652211721080",
        "true, -3000",
        "true, -1",
        "true, 0",
        "true, 1",
        "true, 3000",
        "true, 1652211721080",
        "true, 9223372036854775807",

        "false, -9223372036854775807",
        "false, -1652211721080",
        "false, -3000",
        "false, -1",
        "false, 0",
        "false, 1",
        "false, 3000",
        "false, 1652211721080",
        "false, 9223372036854775807",
    )
    fun canSkipOverUtcDateTimeNode(trustSizeMarkers: Boolean, value: Long) {
        assertCanSkipOverNode(Int64Node(value), trustSizeMarkers)
    }

    @Test
    fun canSerializeAndDeserializeTopLevelInt64Node() {
        val node = Int64Node(Long.MAX_VALUE / 2)

        val bytes = BsonSerializer.serializeBsonNode(node)
        val deserializedNode = BsonDeserializer.deserializeBsonNode(bytes)

        expectThat(deserializedNode).isEqualTo(node)
    }
}