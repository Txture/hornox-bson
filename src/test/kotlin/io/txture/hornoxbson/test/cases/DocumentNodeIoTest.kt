package io.txture.hornoxbson.test.cases

import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.Int32Node
import io.txture.hornoxbson.model.NodeType
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class DocumentNodeIoTest : IoTest() {

    private val emptyDocumentLengthInBytes = Int.SIZE_BYTES + 1

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeEmptyDocumentNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val bytes = this.serializeSingeNode(DocumentNode(length = emptyDocumentLengthInBytes), sizeMarkersWriterSetting)
        val expectedWrittenSize = when (sizeMarkersWriterSetting) {
            SizeMarkersWriterSetting.WRITE_MINUS_1 -> -1
            SizeMarkersWriterSetting.TRUST_DOCUMENT -> emptyDocumentLengthInBytes
            SizeMarkersWriterSetting.RECOMPUTE -> emptyDocumentLengthInBytes
        }
        expectThat(bytes) {
            get { this.size }.isEqualTo(emptyDocumentLengthInBytes)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(0) }.isEqualTo(expectedWrittenSize) // the size includes the null terminator, so we have +1 byte.
                get { this.get(Int.SIZE_BYTES) }.isEqualTo(0x00) // the only byte we have is the null terminator
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeEmptyDocumentNode(trustSizeMarkers: Boolean) {
        val emptyTextNodeBinary = ByteArray(emptyDocumentLengthInBytes) { 0x00.toByte() }
        val buffer = ByteBuffer.wrap(emptyTextNodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(emptyDocumentLengthInBytes)
        val node = this.deserializeSingleNode(emptyTextNodeBinary, 0x03)
        expectThat(node).isA<DocumentNode>().and {
            get { this.length }.isEqualTo(emptyDocumentLengthInBytes)
            get { this.fields }.isEmpty()
            get { this.size }.isEqualTo(0)
        }
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeNonEmptyDocumentNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val keyFirstName = "firstName"
        val keyLastName = "lastName"
        val keyAge = "age"

        val keyFirstNameUtf8Bytes = keyFirstName.toByteArray()
        val keyLastNameUtf8Bytes = keyLastName.toByteArray()
        val keyAgeUtf8Bytes = keyAge.toByteArray()

        val valueFirstName = "John"
        val valueLastName = "Doe"
        val valueAge = 42

        val node = DocumentNode().also {
            it[keyFirstName] = TextNode(valueFirstName)
            it[keyLastName] = TextNode(valueLastName)
            it[keyAge] = Int32Node(valueAge)
        }

        val expectedSize = Int.SIZE_BYTES + // size of document
            // NEXT ENTRY
            1 +  // firstname fingerprint
            keyFirstNameUtf8Bytes.size + 1 + // firstname key (and null terminator)
            Int.SIZE_BYTES + // firstname value size
            valueFirstName.toByteArray().size +  // firstname value
            1 + // first name null terminator
            // NEXT ENTRY
            1 + // lastname fingerprint
            keyLastNameUtf8Bytes.size + 1 + // lastname key (and null terminator)
            Int.SIZE_BYTES + // lastname value size
            valueLastName.toByteArray().size + // lastname value
            1 +  // last name null terminator
            // NEXT ENTRY
            1 + // age fingerprint
            keyAgeUtf8Bytes.size + 1 + // age key (and null terminator)
            Int.SIZE_BYTES + // age value size (no null terminator for Int32 nodes)
            // NEXT ENTRY
            1 // null terminator of the document

        node.length = expectedSize
        val bytes = this.serializeSingeNode(node, sizeMarkersWriterSetting)

        val sizeToAssert = when(sizeMarkersWriterSetting){
            SizeMarkersWriterSetting.WRITE_MINUS_1 -> -1
            SizeMarkersWriterSetting.TRUST_DOCUMENT -> expectedSize
            SizeMarkersWriterSetting.RECOMPUTE -> expectedSize
        }

        expectThat(bytes) {
            get { this.size }.isEqualTo(expectedSize)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(0) }.isEqualTo(sizeToAssert)
                get { this.get(Int.SIZE_BYTES) }.isEqualTo(NodeType.TEXT.fingerprintByte)
                get { this.get(expectedSize-1) }.isEqualTo(0x00.toByte())
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverEmptyDocumentNode(trustSizeMarkers: Boolean) {
        assertCanSkipOverNode(DocumentNode(length = emptyDocumentLengthInBytes), trustSizeMarkers)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverNonEmptyDocumentNode(trustSizeMarkers: Boolean) {
        val node = DocumentNode().also {
            it["firstName"] = TextNode("John")
            it["lastName"] = TextNode("Doe")
            it["age"] = Int32Node(42)
        }
        assertCanSkipOverNode(node, trustSizeMarkers)
    }
}