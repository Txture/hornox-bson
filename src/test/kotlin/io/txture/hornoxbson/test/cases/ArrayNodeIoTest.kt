package io.txture.hornoxbson.test.cases

import io.txture.hornoxbson.BsonDeserializer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting
import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.model.*
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class ArrayNodeIoTest : IoTest() {

    private val emptyArrayNodeLengthInBytes = Int.SIZE_BYTES + 1

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation() {
        // created by MongoDB BSON serializer implementation.
        val reference = "330000000461001b00000002300004000000666f6f000231000400000062617200000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = ArrayNode(TextNode("foo"), TextNode("bar"))
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeEmptyArrayNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val bytes = this.serializeSingeNode(ArrayNode(length = emptyArrayNodeLengthInBytes), sizeMarkersWriterSetting)
        val expectedWrittenSize = when (sizeMarkersWriterSetting) {
            SizeMarkersWriterSetting.WRITE_MINUS_1 -> -1
            SizeMarkersWriterSetting.TRUST_DOCUMENT -> emptyArrayNodeLengthInBytes
            SizeMarkersWriterSetting.RECOMPUTE -> emptyArrayNodeLengthInBytes
        }
        expectThat(bytes) {
            get { this.size }.isEqualTo(emptyArrayNodeLengthInBytes)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(0) }.isEqualTo(expectedWrittenSize) // the size includes the null terminator, so we have +1 byte.
                get { this.get(Int.SIZE_BYTES) }.isEqualTo(0x00) // the only byte we have is the null terminator
            }
        }
    }

    @Test
    fun canDeserializeEmptyArrayNode() {
        val emptyTextNodeBinary = ByteArray(emptyArrayNodeLengthInBytes) { 0x00.toByte() }
        val buffer = ByteBuffer.wrap(emptyTextNodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(emptyArrayNodeLengthInBytes)
        val node = this.deserializeSingleNode(emptyTextNodeBinary, 0x04)
        expectThat(node).isA<ArrayNode>().and {
            get { this.length }.isEqualTo(emptyArrayNodeLengthInBytes)
            get { this.fields }.isEmpty()
            get { this.size }.isEqualTo(0)
        }
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeNonEmptyArrayNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val node = ArrayNode().also {
            it.add(Int32Node(1))
            it.add(Int32Node(2))
            it.add(Int32Node(3))
        }

        val expectedSize = Int.SIZE_BYTES + // size of array
            // NEXT ENTRY
            1 +  // entry[0] fingerprint
            2 + // key "0" (and null terminator)
            Int.SIZE_BYTES + // entry[0] value
            // NEXT ENTRY
            1 + // entry[1] fingerprint
            2 + // key "1" (and null terminator)
            Int.SIZE_BYTES + // entry[1] value
            // NEXT ENTRY
            1 + // entry[2] fingerprint
            2 + // key "2" (and null terminator)
            Int.SIZE_BYTES + // entry[2] value
            // NEXT ENTRY
            1 // null terminator of the document

        node.length = expectedSize
        val bytes = this.serializeSingeNode(node, sizeMarkersWriterSetting)

        val sizeToAssert = when (sizeMarkersWriterSetting) {
            SizeMarkersWriterSetting.WRITE_MINUS_1 -> -1
            SizeMarkersWriterSetting.TRUST_DOCUMENT -> expectedSize
            SizeMarkersWriterSetting.RECOMPUTE -> expectedSize
        }

        expectThat(bytes) {
            get { this.size }.isEqualTo(expectedSize)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(0) }.isEqualTo(sizeToAssert)
                get { this.get(Int.SIZE_BYTES) }.isEqualTo(NodeType.INT32.fingerprintByte)
                get { this.get(expectedSize - 1) }.isEqualTo(0x00.toByte())
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverEmptyArrayNode(trustSizeMarkers: Boolean) {
        assertCanSkipOverNode(ArrayNode(length = emptyArrayNodeLengthInBytes), trustSizeMarkers)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverNonEmptyArrayNode(trustSizeMarkers: Boolean) {
        val node = ArrayNode().also {
            it.add(Int32Node(1))
            it.add(Int32Node(2))
            it.add(Int32Node(3))
        }
        assertCanSkipOverNode(node, trustSizeMarkers)
    }

    @Test
    fun canSerializeAndDeserializeTopLevelArrayNode() {
        val node = ArrayNode(Int32Node(1), Int32Node(2), Int32Node(3))

        val bytes = BsonSerializer.serializeBsonNode(node)
        val deserializedNode = BsonDeserializer.deserializeBsonNode(bytes)

        expectThat(deserializedNode).isEqualTo(node)
    }
}