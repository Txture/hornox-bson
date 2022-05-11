package io.txture.hornoxbson.test.cases

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class TextNodeIoTest: IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "380000000261001c0000004c6f72656d20697073756d20646f6c6f722073697420616d657421000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = TextNode("Lorem ipsum dolor sit amet!")
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(BsonSerializer.SizeMarkersWriterSetting::class)
    fun canSerializeEmptyTextNode(sizeMarkersWriterSetting: BsonSerializer.SizeMarkersWriterSetting) {
        val bytes = this.serializeSingeNode(TextNode(""), sizeMarkersWriterSetting)
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
    fun canDeserializeEmptyTextNode(trustSizeMarkers: Boolean) {
        val emptyTextNodeBinary = ByteArray(Int.SIZE_BYTES + 1){ 0x00.toByte() }
        val buffer = ByteBuffer.wrap(emptyTextNodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(1)
        val node = this.deserializeSingleNode(emptyTextNodeBinary, 0x02)
        expectThat(node).isA<TextNode>().get { this.value }.isEmpty()
    }

    @ParameterizedTest
    @EnumSource(BsonSerializer.SizeMarkersWriterSetting::class)
    fun canSerializeNonEmptyTextNode(sizeMarkersWriterSetting: BsonSerializer.SizeMarkersWriterSetting) {
        val textContent = "Lorem Ipsum\nDolor Sit Amet!"
        val bytes = this.serializeSingeNode(TextNode(textContent), sizeMarkersWriterSetting)
        val textUtf8 = textContent.toByteArray()
        expectThat(bytes) {
            get { this.size }.isEqualTo(Int.SIZE_BYTES + textUtf8.size + 1)
            get { this }.isEqualTo(intToBytes(textUtf8.size+1) + textUtf8 + 0x00.toByte())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverEmptyTextNode(trustSizeMarkers: Boolean){
        assertCanSkipOverNode(TextNode(""), trustSizeMarkers)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverNonEmptyTextNode(trustSizeMarkers: Boolean){
        assertCanSkipOverNode(TextNode("Lorem Ipsum\nDolor Sit Amet!"), trustSizeMarkers)
    }


}