package io.txture.hornoxbson.test.cases

import jakarta.json.JsonValue
import org.junit.jupiter.api.fail
import io.txture.hornoxbson.BsonDeserializer
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting
import io.txture.hornoxbson.model.BsonNode
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class IoTest {

    protected fun deserializeSingleNode(byteArray: ByteArray, fingerprint: Byte): JsonValue {
        // create a document byte array
        val documentBytes = ByteArrayOutputStream().use { baos ->
            // write size of the document
            val totalSize = Int.SIZE_BYTES + // document size field
                1 + // fingerprint byte
                2 + // string "t" with null terminator
                byteArray.size +
                1 // document null terminator
            baos.writeBytes(intToBytes(totalSize))
            baos.write(fingerprint.toInt())
            baos.write("t".toByteArray())
            baos.write(0x00) // terminate the field name
            baos.writeBytes(byteArray)
            baos.write(0x00) // terminate the document
            baos.toByteArray()
        }
        val document = BsonDeserializer.deserializeBsonDocument(documentBytes)
        return document["t"]
            ?: fail("Wrapper document didn't contain a value for test field 't'!")
    }

    protected fun serializeSingeNode(node: BsonNode, sizeMarkersWriterSetting: SizeMarkersWriterSetting): ByteArray {
        val documentNode = DocumentNode()
        documentNode.fields["t"] = node
        val array = BsonSerializer.serializeBsonDocument(documentNode, sizeMarkersWriterSetting)
        // check that the fingerprint has been written correctly (after the document size)
        expectThat(array[Int.SIZE_BYTES]).isEqualTo(node.fingerprintByte)
        val startPosition = Int.SIZE_BYTES + // skip the outer document size
            1 + // skip the element fingerprint
            2 // skip the string "t" and the 0x00 byte that terminates the string
        return array.sliceArray(startPosition until array.size - 1 /* document null terminator */)
    }

    protected fun assertCanSkipOverNode(node: BsonNode, trustSizeMarkers: Boolean) {
        val documentNode = DocumentNode()
        documentNode.fields["a"] = node
        documentNode.fields["t"] = TextNode("Success!")
        val array = BsonSerializer.serializeBsonDocument(documentNode, SizeMarkersWriterSetting.RECOMPUTE)
        val extracted = BsonDeserializer.extractBsonNode(array, listOf("t"), trustSizeMarkers)
        expectThat(extracted).isA<TextNode>().get { this.value }.isEqualTo("Success!")
    }

    protected fun intToBytes(value: Int): ByteArray {
        val bb = ByteBuffer.allocate(Int.SIZE_BYTES)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        bb.putInt(value)
        return bb.array()
    }

}