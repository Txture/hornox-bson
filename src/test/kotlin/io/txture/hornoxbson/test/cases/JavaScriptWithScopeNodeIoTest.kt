package io.txture.hornoxbson.test.cases

import io.txture.hornoxbson.BsonDeserializer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.model.*
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class JavaScriptWithScopeNodeIoTest : IoTest() {

    private val emptyStringWithSizeLengthInBytes = Int.SIZE_BYTES /* size integer */ + 1 /* end-of-string null terminal */
    private val emptyDocumentLengthInBytes = Int.SIZE_BYTES  /* document size integer */ + 1 /* end-of-document null terminal */


    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "260000000f61000e000000010000000005000000000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = JavaScriptWithScopeNode("", DocumentNode())
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(BsonSerializer.SizeMarkersWriterSetting::class)
    fun canSerializeEmptyJavaScriptWithScopeNode(sizeMarkersWriterSetting: BsonSerializer.SizeMarkersWriterSetting) {
        val bytes = this.serializeSingeNode(JavaScriptWithScopeNode("", DocumentNode(length = emptyDocumentLengthInBytes)), sizeMarkersWriterSetting)

        val expectedContextDocumentSize = when(sizeMarkersWriterSetting){
            BsonSerializer.SizeMarkersWriterSetting.WRITE_MINUS_1 -> -1
            BsonSerializer.SizeMarkersWriterSetting.TRUST_DOCUMENT -> emptyDocumentLengthInBytes
            BsonSerializer.SizeMarkersWriterSetting.RECOMPUTE -> emptyDocumentLengthInBytes
        }

        val expectedOverallNodeSize = when(sizeMarkersWriterSetting){
            BsonSerializer.SizeMarkersWriterSetting.WRITE_MINUS_1 -> -1
            BsonSerializer.SizeMarkersWriterSetting.TRUST_DOCUMENT -> Int.SIZE_BYTES + emptyStringWithSizeLengthInBytes + emptyDocumentLengthInBytes
            BsonSerializer.SizeMarkersWriterSetting.RECOMPUTE -> Int.SIZE_BYTES + emptyStringWithSizeLengthInBytes + emptyDocumentLengthInBytes
        }

        expectThat(bytes) {
            get { this.size }.isEqualTo(Int.SIZE_BYTES + emptyStringWithSizeLengthInBytes + emptyDocumentLengthInBytes)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(0) }.isEqualTo(expectedOverallNodeSize) // size = whole size of the node
                get { this.getInt(Int.SIZE_BYTES) }.isEqualTo(1) // string of length 1 (containing only the null terminator)
                get { this.get(Int.SIZE_BYTES*2)}.isEqualTo(0x00) // null terminal of the script string
                get { this.getInt(Int.SIZE_BYTES*2 + 1)}.isEqualTo(expectedContextDocumentSize) // length of the (empty) context document
                get { this.get(Int.SIZE_BYTES*2 + 1 + Int.SIZE_BYTES)}.isEqualTo(0x00) // null terminal of the context document
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeEmptyJavaScriptWithScopeNode(trustSizeMarkers: Boolean) {
        val overallNodeSize = Int.SIZE_BYTES + emptyStringWithSizeLengthInBytes + emptyDocumentLengthInBytes
        val emptyJavaScriptWithScopeNodeBinary = ByteArray(overallNodeSize)
        val buffer = ByteBuffer.wrap(emptyJavaScriptWithScopeNodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(overallNodeSize)
        buffer.putInt(1) // string length (incl. null terminator)
        buffer.put(0x00)
        buffer.putInt(emptyDocumentLengthInBytes)
        buffer.put(0x00)
        val node = this.deserializeSingleNode(emptyJavaScriptWithScopeNodeBinary, 0x0F)
        expectThat(node).isA<JavaScriptWithScopeNode>().get { this.value }.isEmpty()
    }

    @ParameterizedTest
    @EnumSource(BsonSerializer.SizeMarkersWriterSetting::class)
    fun canSerializeNonEmptyJavaScriptWithScopeNode(sizeMarkersWriterSetting: BsonSerializer.SizeMarkersWriterSetting) {
        val textContent = "let x = 3;\n x *= 2;\n Console.log(x);"
        val context = DocumentNode(length = emptyDocumentLengthInBytes)
        val bytes = this.serializeSingeNode(JavaScriptWithScopeNode(textContent, context), sizeMarkersWriterSetting)
        val textUtf8 = textContent.toByteArray()
        val arr = ByteArray(Int.SIZE_BYTES + Int.SIZE_BYTES + textUtf8.size + 1 + Int.SIZE_BYTES + 1)
        val buffer = ByteBuffer.wrap(arr).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(when(sizeMarkersWriterSetting){
            BsonSerializer.SizeMarkersWriterSetting.WRITE_MINUS_1 -> -1
            BsonSerializer.SizeMarkersWriterSetting.TRUST_DOCUMENT -> arr.size
            BsonSerializer.SizeMarkersWriterSetting.RECOMPUTE -> arr.size
        })
        buffer.putInt(textUtf8.size+1) // string length (incl. null terminator)
        buffer.put(textUtf8)
        buffer.put(0x00)
        buffer.putInt(when(sizeMarkersWriterSetting){
            BsonSerializer.SizeMarkersWriterSetting.WRITE_MINUS_1 -> -1
            BsonSerializer.SizeMarkersWriterSetting.TRUST_DOCUMENT -> emptyDocumentLengthInBytes
            BsonSerializer.SizeMarkersWriterSetting.RECOMPUTE -> emptyDocumentLengthInBytes
        })
        buffer.put(0x00)
        expectThat(bytes) {
            // get { this.size }.isEqualTo(arr.size)
            get { this }.isEqualTo(arr)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverEmptyJavaScriptWithScopeNode(trustSizeMarkers: Boolean) {
        val context = DocumentNode(length = emptyDocumentLengthInBytes)
        assertCanSkipOverNode(JavaScriptWithScopeNode("", context), trustSizeMarkers)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverNonEmptyJavaScriptWithScopeNode(trustSizeMarkers: Boolean) {
        val context = DocumentNode()
        assertCanSkipOverNode(JavaScriptWithScopeNode("let x = 3;\n x *= 2;\n Console.log(x);", context), trustSizeMarkers)
    }

    @Test
    fun canSerializeAndDeserializeTopLevelJavaScriptWithScopeNode() {
        val node = JavaScriptWithScopeNode("let x = 3 * y", DocumentNode(fields = mutableMapOf("y" to Int32Node(5))))

        val bytes = BsonSerializer.serializeBsonNode(node)
        val deserializedNode = BsonDeserializer.deserializeBsonNode(bytes)

        expectThat(deserializedNode).isEqualTo(node)
    }
}