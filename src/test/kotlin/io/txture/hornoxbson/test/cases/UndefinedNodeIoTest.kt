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
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.TextNode
import io.txture.hornoxbson.model.TimestampNode
import io.txture.hornoxbson.model.UndefinedNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@Tag("UnitTest")
class UndefinedNodeIoTest : IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "180000000661000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = UndefinedNode
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeUndefinedNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting, ) {
        val bytes = this.serializeSingeNode(UndefinedNode, sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(0)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeUndefinedNode(trustSizeMarkers: Boolean) {
        val undefinedNodeByteArray = ByteArray(0)
        val node = this.deserializeSingleNode(undefinedNodeByteArray, 0x06)
        expectThat(node).isA<UndefinedNode>()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverDoubleNode(trustSizeMarkers: Boolean) {
        assertCanSkipOverNode(UndefinedNode, trustSizeMarkers)
    }

    @Test
    fun canSerializeAndDeserializeTopLevelUndefinedNode() {
        val node = UndefinedNode

        val bytes = BsonSerializer.serializeBsonNode(node)
        val deserializedNode = BsonDeserializer.deserializeBsonNode(bytes)

        expectThat(deserializedNode).isEqualTo(node)
    }
}