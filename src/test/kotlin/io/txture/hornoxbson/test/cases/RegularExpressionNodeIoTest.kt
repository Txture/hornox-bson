package io.txture.hornoxbson.test.cases

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting
import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.RegularExpressionNode
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class RegularExpressionNodeIoTest : IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation() {
        // created by MongoDB BSON serializer implementation.
        val reference = "320000000b61005b48685d656c6c6f205b57775d6f726c642100696c6d737578000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = RegularExpressionNode("[Hh]ello [Ww]orld!", "ilmsux")
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeEmptyRegularExpressionNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val bytes = this.serializeSingeNode(RegularExpressionNode("", ""), sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(2) // two null terminators
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.get(0) }.isEqualTo(0x00) // null terminator of the regex string
                get { this.get(1) }.isEqualTo(0x00) // null terminator of the options string
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeEmptyRegularExpressionNode(trustSizeMarkers: Boolean) {
        val emptyRegexNodeBinary = ByteArray(2)
        val node = this.deserializeSingleNode(emptyRegexNodeBinary, 0x0B)
        expectThat(node).isA<RegularExpressionNode>().and {
            get { this.regularExpression }.isEmpty()
            get { this.options }.isEmpty()
        }
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeNonEmptyRegularExpressionNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val regex = "[Hh]ello [Ww]orld!"
        val options = "ilmsux"
        val bytes = this.serializeSingeNode(RegularExpressionNode(regex, options), sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(regex.toByteArray().size + 1 + options.toByteArray().size + 1)
            get { this }.isEqualTo(regex.toByteArray() + 0x00.toByte() + options.toByteArray() + 0x00.toByte())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverEmptyRegularExpressionNode(trustSizeMarkers: Boolean) {
        assertCanSkipOverNode(RegularExpressionNode("", ""), trustSizeMarkers)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverNonEmptyRegularExpressionNode(trustSizeMarkers: Boolean) {
        assertCanSkipOverNode(RegularExpressionNode(regex = "[Hh]ello [Ww]orld!", options = "ilmsux"), trustSizeMarkers)
    }


}