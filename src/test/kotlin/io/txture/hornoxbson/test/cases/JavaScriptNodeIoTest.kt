package io.txture.hornoxbson.test.cases

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.JavaScriptNode
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class JavaScriptNodeIoTest: IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "410000000d6100250000006c65742078203d20333b0a2078202a3d20323b0a20436f6e736f6c652e6c6f672878293b000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = JavaScriptNode("let x = 3;\n x *= 2;\n Console.log(x);")
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(BsonSerializer.SizeMarkersWriterSetting::class)
    fun canSerializeEmptyJavaScriptNode(sizeMarkersWriterSetting: BsonSerializer.SizeMarkersWriterSetting) {
        val bytes = this.serializeSingeNode(JavaScriptNode(""), sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(Int.SIZE_BYTES + 1)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(0) }.isEqualTo(1) // the size includes the null terminator, so we have 1 byte.
                get { this.get(Int.SIZE_BYTES) }.isEqualTo(0x00) // the only byte we have is the null terminator
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeEmptyJavaScriptNode(trustSizeMarkers: Boolean) {
        val emptyJavaScriptNodeBinary = ByteArray(Int.SIZE_BYTES + 1){ 0x00.toByte() }
        val buffer = ByteBuffer.wrap(emptyJavaScriptNodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(1)
        val node = this.deserializeSingleNode(emptyJavaScriptNodeBinary, 0x0D)
        expectThat(node).isA<JavaScriptNode>().get { this.value }.isEmpty()
    }

    @ParameterizedTest
    @EnumSource(BsonSerializer.SizeMarkersWriterSetting::class)
    fun canSerializeNonEmptyJavaScriptNode(sizeMarkersWriterSetting: BsonSerializer.SizeMarkersWriterSetting) {
        val textContent = "let x = 3;\n x *= 2;\n Console.log(x);"
        val bytes = this.serializeSingeNode(JavaScriptNode(textContent), sizeMarkersWriterSetting)
        val textUtf8 = textContent.toByteArray()
        expectThat(bytes) {
            get { this.size }.isEqualTo(Int.SIZE_BYTES + textUtf8.size + 1)
            get { this }.isEqualTo(intToBytes(textUtf8.size+1) + textUtf8 + 0x00.toByte())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverEmptyJavaScriptNode(trustSizeMarkers: Boolean){
        assertCanSkipOverNode(JavaScriptNode(""), trustSizeMarkers)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverNonEmptyJavaScriptNode(trustSizeMarkers: Boolean){
        assertCanSkipOverNode(JavaScriptNode("let x = 3;\n x *= 2;\n Console.log(x);"), trustSizeMarkers)
    }


}