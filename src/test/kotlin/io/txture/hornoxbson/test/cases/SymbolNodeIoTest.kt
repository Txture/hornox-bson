package io.txture.hornoxbson.test.cases

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.SymbolNode
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class SymbolNodeIoTest: IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "250000000e6100090000003132332d666f6f21000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = SymbolNode("123-foo!")
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(BsonSerializer.SizeMarkersWriterSetting::class)
    fun canSerializeEmptySymbolNode(sizeMarkersWriterSetting: BsonSerializer.SizeMarkersWriterSetting) {
        val bytes = this.serializeSingeNode(SymbolNode(""), sizeMarkersWriterSetting)
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
    fun canDeserializeEmptySymbolNode(trustSizeMarkers: Boolean) {
        val emptySymbolNodeBinary = ByteArray(Int.SIZE_BYTES + 1){ 0x00.toByte() }
        val buffer = ByteBuffer.wrap(emptySymbolNodeBinary).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(1)
        val node = this.deserializeSingleNode(emptySymbolNodeBinary, 0x0E)
        expectThat(node).isA<SymbolNode>().get { this.value }.isEmpty()
    }

    @ParameterizedTest
    @EnumSource(BsonSerializer.SizeMarkersWriterSetting::class)
    fun canSerializeNonEmptySymbolNode(sizeMarkersWriterSetting: BsonSerializer.SizeMarkersWriterSetting) {
        val textContent = "mySymbol"
        val bytes = this.serializeSingeNode(SymbolNode(textContent), sizeMarkersWriterSetting)
        val textUtf8 = textContent.toByteArray()
        expectThat(bytes) {
            get { this.size }.isEqualTo(Int.SIZE_BYTES + textUtf8.size + 1)
            get { this }.isEqualTo(intToBytes(textUtf8.size+1) + textUtf8 + 0x00.toByte())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverEmptySymbolNode(trustSizeMarkers: Boolean){
        assertCanSkipOverNode(SymbolNode(""), trustSizeMarkers)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverNonEmptySymbolNode(trustSizeMarkers: Boolean){
        assertCanSkipOverNode(SymbolNode("mySymbol"), trustSizeMarkers)
    }


}