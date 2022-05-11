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
import io.txture.hornoxbson.model.FalseNode
import io.txture.hornoxbson.model.TextNode
import io.txture.hornoxbson.model.TrueNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@Tag("UnitTest")
class BooleanNodeIoTest : IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "1d00000008610001086200000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = TrueNode
        doc2["b"] = FalseNode
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeTrueNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val bytes = this.serializeSingeNode(TrueNode, sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(1)
            get { this[0] }.isEqualTo(0x01)
        }
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeFalseNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val bytes = this.serializeSingeNode(FalseNode, sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(1)
            get { this[0] }.isEqualTo(0x00)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeTrueNode(trustSizeMarkers: Boolean) {
        val bytes = ByteArray(1)
        bytes[0] = 0x01
        val node = this.deserializeSingleNode(bytes, 0x08)
        expectThat(node).isA<TrueNode>().and {
            get { this.value }.isTrue()
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeFalseNode(trustSizeMarkers: Boolean) {
        val bytes = ByteArray(1)
        bytes[0] = 0x00
        val node = this.deserializeSingleNode(bytes, 0x08)
        expectThat(node).isA<FalseNode>().and {
            get { this.value }.isFalse()
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverTrueNode(trustSizeMarkers: Boolean) {
        assertCanSkipOverNode(TrueNode, trustSizeMarkers)
    }


    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverFalseNode(trustSizeMarkers: Boolean) {
        assertCanSkipOverNode(FalseNode, trustSizeMarkers)
    }

}