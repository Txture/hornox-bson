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
import io.txture.hornoxbson.model.MaxKeyNode
import io.txture.hornoxbson.model.NullNode
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@Tag("UnitTest")
class NullNodeIoTest : IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "180000000a61000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = NullNode
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeNullNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting, ) {
        val bytes = this.serializeSingeNode(NullNode, sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(0)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeNullNode(trustSizeMarkers: Boolean) {
        val undefinedNodeByteArray = ByteArray(0)
        val node = this.deserializeSingleNode(undefinedNodeByteArray, 0x0A)
        expectThat(node).isA<NullNode>()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverNullNode(trustSizeMarkers: Boolean) {
        assertCanSkipOverNode(NullNode, trustSizeMarkers)
    }

    @Test
    fun canSerializeAndDeserializeTopLevelNullNode() {
        val node = NullNode

        val bytes = BsonSerializer.serializeBsonNode(node)
        val deserializedNode = BsonDeserializer.deserializeBsonNode(bytes)

        expectThat(deserializedNode).isEqualTo(node)
    }
}