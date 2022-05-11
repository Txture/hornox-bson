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
import io.txture.hornoxbson.model.MinKeyNode
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@Tag("UnitTest")
class MinKeyNodeIoTest : IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "18000000ff61000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = MinKeyNode
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeMinKeyNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting, ) {
        val bytes = this.serializeSingeNode(MinKeyNode, sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(0)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeMinKeyNode(trustSizeMarkers: Boolean) {
        val undefinedNodeByteArray = ByteArray(0)
        val node = this.deserializeSingleNode(undefinedNodeByteArray, 0xFF.toByte())
        expectThat(node).isA<MinKeyNode>()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverMinKeyNode(trustSizeMarkers: Boolean) {
        assertCanSkipOverNode(MinKeyNode, trustSizeMarkers)
    }
}