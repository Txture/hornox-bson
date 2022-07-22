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
import io.txture.hornoxbson.model.DoubleNode
import io.txture.hornoxbson.model.Int32Node
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class DoubleNodeIoTest : IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "200000000161006f1283c0ca2109400274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = DoubleNode(3.1415)
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @CsvSource(
        "WRITE_MINUS_1, 3.1415",
        "WRITE_MINUS_1, -1.0",
        "WRITE_MINUS_1, 1.0",
        "WRITE_MINUS_1, -3.1415",
        "WRITE_MINUS_1, 0.0",

        "TRUST_DOCUMENT, 3.1415",
        "TRUST_DOCUMENT, -1.0",
        "TRUST_DOCUMENT, 1.0",
        "TRUST_DOCUMENT, -3.1415",
        "TRUST_DOCUMENT, 0.0",

        "RECOMPUTE, 3.1415",
        "RECOMPUTE, -1.0",
        "RECOMPUTE, 1.0",
        "RECOMPUTE, -3.1415",
        "RECOMPUTE, 0.0",
    )
    fun canSerializeDoubleNode(
        sizeMarkersWriterSetting: SizeMarkersWriterSetting,
        value: Double
    ) {
        val bytes = this.serializeSingeNode(DoubleNode(value), sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(Double.SIZE_BYTES)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getDouble(0) }.isEqualTo(value)
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "true, 3.1415",
        "true, -1.0",
        "true, 1.0",
        "true, -3.1415",
        "true, 0.0",

        "false, 3.1415",
        "false, -1.0",
        "false, 1.0",
        "false, -3.1415",
        "false, 0.0",
    )
    fun canDeserializeDoubleNode(trustSizeMarkers: Boolean, value: Double) {
        val doubleNodeBinary = ByteArray(Double.SIZE_BYTES) { 0x00.toByte() }
        val buffer = ByteBuffer.wrap(doubleNodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putDouble(value)
        val node = this.deserializeSingleNode(doubleNodeBinary, 0x01)
        expectThat(node).isA<DoubleNode>().get { this.value }.isEqualTo(value)
    }

    @ParameterizedTest
    @CsvSource(
        "true, 3.1415",
        "true, -1.0",
        "true, 1.0",
        "true, -3.1415",
        "true, 0.0",

        "false, 3.1415",
        "false, -1.0",
        "false, 1.0",
        "false, -3.1415",
        "false, 0.0",
    )
    fun canSkipOverDoubleNode(trustSizeMarkers: Boolean, value: Double) {
        assertCanSkipOverNode(DoubleNode(value), trustSizeMarkers)
    }

    @Test
    fun canSerializeAndDeserializeTopLevelDoubleNode() {
        val node = DoubleNode(3.1415)

        val bytes = BsonSerializer.serializeBsonNode(node)
        val deserializedNode = BsonDeserializer.deserializeBsonNode(bytes)

        expectThat(deserializedNode).isEqualTo(node)
    }
}