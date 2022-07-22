package io.txture.hornoxbson.test.cases

import io.txture.hornoxbson.BsonDeserializer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting
import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.model.*
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class BinaryNodeIoTest : IoTest() {

    private val emptyBinaryNodeLengthInBytes = Int.SIZE_BYTES + 1 // +1 for binary subtype


    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation() {
        // created by MongoDB BSON serializer implementation.
        val reference = "25000000056100080000000003030303030303030274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = BinaryNode(ByteArray(8) { 0x03 }, BinarySubtype.GENERIC)
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @CsvSource(
        "WRITE_MINUS_1, GENERIC",
        "WRITE_MINUS_1, FUNCTION",
        "WRITE_MINUS_1, UUID",
        "WRITE_MINUS_1, MD5",
        "WRITE_MINUS_1, ENCRYPTED",
        "WRITE_MINUS_1, COMPRESSED",
        "WRITE_MINUS_1, USER_DEFINED",

        "TRUST_DOCUMENT, GENERIC",
        "TRUST_DOCUMENT, FUNCTION",
        "TRUST_DOCUMENT, UUID",
        "TRUST_DOCUMENT, MD5",
        "TRUST_DOCUMENT, ENCRYPTED",
        "TRUST_DOCUMENT, COMPRESSED",
        "TRUST_DOCUMENT, USER_DEFINED",

        "RECOMPUTE, GENERIC",
        "RECOMPUTE, FUNCTION",
        "RECOMPUTE, UUID",
        "RECOMPUTE, MD5",
        "RECOMPUTE, ENCRYPTED",
        "RECOMPUTE, COMPRESSED",
        "RECOMPUTE, USER_DEFINED",
    )
    fun canSerializeEmptyBinaryNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting, subType: BinarySubtype) {
        val bytes = this.serializeSingeNode(BinaryNode(value = ByteArray(0), subtype = subType), sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(emptyBinaryNodeLengthInBytes)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(0) }.isEqualTo(0) // the size only encompasses the length of the byte array, which is 0
                get { this.get(Int.SIZE_BYTES) }.isEqualTo(subType.byte)
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "true, GENERIC",
        "true, FUNCTION",
        "true, UUID",
        "true, MD5",
        "true, ENCRYPTED",
        "true, COMPRESSED",
        "true, USER_DEFINED",

        "false, GENERIC",
        "false, FUNCTION",
        "false, UUID",
        "false, MD5",
        "false, ENCRYPTED",
        "false, COMPRESSED",
        "false, USER_DEFINED",
    )
    fun canDeserializeEmptyBinaryNode(trustSizeMarkers: Boolean, subType: BinarySubtype) {
        val emptyTextNodeBinary = ByteArray(emptyBinaryNodeLengthInBytes) { 0x00.toByte() }
        val buffer = ByteBuffer.wrap(emptyTextNodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(0) // the byte array lenght is zero, and that's all we care about here
        buffer.put(subType.byte)
        val node = this.deserializeSingleNode(emptyTextNodeBinary, 0x05)
        expectThat(node).isA<BinaryNode>().and {
            get { this.value.size }.isEqualTo(0)
            get { this.subtype }.isEqualTo(subType)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "WRITE_MINUS_1, GENERIC",
        "WRITE_MINUS_1, FUNCTION",
        "WRITE_MINUS_1, UUID",
        "WRITE_MINUS_1, MD5",
        "WRITE_MINUS_1, ENCRYPTED",
        "WRITE_MINUS_1, COMPRESSED",
        "WRITE_MINUS_1, USER_DEFINED",

        "TRUST_DOCUMENT, GENERIC",
        "TRUST_DOCUMENT, FUNCTION",
        "TRUST_DOCUMENT, UUID",
        "TRUST_DOCUMENT, MD5",
        "TRUST_DOCUMENT, ENCRYPTED",
        "TRUST_DOCUMENT, COMPRESSED",
        "TRUST_DOCUMENT, USER_DEFINED",

        "RECOMPUTE, GENERIC",
        "RECOMPUTE, FUNCTION",
        "RECOMPUTE, UUID",
        "RECOMPUTE, MD5",
        "RECOMPUTE, ENCRYPTED",
        "RECOMPUTE, COMPRESSED",
        "RECOMPUTE, USER_DEFINED",
    )
    fun canSerializeNonEmptyBinaryNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting, subType: BinarySubtype) {
        val content = ByteArray(Int.SIZE_BYTES * 3)
        ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).putInt(0, 1).putInt(Int.SIZE_BYTES, 2).putInt(Int.SIZE_BYTES * 2, 3)
        val node = BinaryNode(value = content, subtype = subType)

        val expectedSize = Int.SIZE_BYTES + // size of array
            Int.SIZE_BYTES * 3 + // byte array itself
            1 // subtype

        val bytes = this.serializeSingeNode(node, sizeMarkersWriterSetting)

        expectThat(bytes) {
            get { this.size }.isEqualTo(expectedSize)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(0) }.isEqualTo(content.size)
                get { this.get(Int.SIZE_BYTES) }.isEqualTo(subType.byte)
                get { this.getInt(Int.SIZE_BYTES + 1) }.isEqualTo(1)
                get { this.getInt(Int.SIZE_BYTES * 2 + 1) }.isEqualTo(2)
                get { this.getInt(Int.SIZE_BYTES * 3 + 1) }.isEqualTo(3)
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "true, GENERIC",
        "true, FUNCTION",
        "true, UUID",
        "true, MD5",
        "true, ENCRYPTED",
        "true, COMPRESSED",
        "true, USER_DEFINED",

        "false, GENERIC",
        "false, FUNCTION",
        "false, UUID",
        "false, MD5",
        "false, ENCRYPTED",
        "false, COMPRESSED",
        "false, USER_DEFINED",
    )
    fun canSkipOverEmptyBinaryNode(trustSizeMarkers: Boolean, subType: BinarySubtype) {
        assertCanSkipOverNode(BinaryNode(value = ByteArray(0), subtype = subType), trustSizeMarkers)
    }

    @ParameterizedTest
    @CsvSource(
        "true, GENERIC",
        "true, FUNCTION",
        "true, UUID",
        "true, MD5",
        "true, ENCRYPTED",
        "true, COMPRESSED",
        "true, USER_DEFINED",

        "false, GENERIC",
        "false, FUNCTION",
        "false, UUID",
        "false, MD5",
        "false, ENCRYPTED",
        "false, COMPRESSED",
        "false, USER_DEFINED",
    )
    fun canSkipOverNonEmptyBinaryNode(trustSizeMarkers: Boolean, subType: BinarySubtype) {
        val content = ByteArray(Int.SIZE_BYTES * 3)
        ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).putInt(0, 1).putInt(Int.SIZE_BYTES, 2).putInt(Int.SIZE_BYTES * 2, 3)
        val node = BinaryNode(value = content, subtype = subType)

        assertCanSkipOverNode(node, trustSizeMarkers)
    }

    @Test
    fun canSerializeAndDeserializeTopLevelBinaryNode() {
        val node = BinaryNode(byteArrayOf(12, 14, 32), BinarySubtype.COMPRESSED)

        val bytes = BsonSerializer.serializeBsonNode(node)
        val deserializedNode = BsonDeserializer.deserializeBsonNode(bytes)

        expectThat(deserializedNode).isEqualTo(node)
    }
}