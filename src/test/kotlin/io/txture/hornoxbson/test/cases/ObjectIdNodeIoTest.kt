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
import io.txture.hornoxbson.model.NullNode
import io.txture.hornoxbson.model.ObjectIdNode
import io.txture.hornoxbson.model.TextNode
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Tag("UnitTest")
class ObjectIdNodeIoTest : IoTest() {

    @Test
    fun compareSerializedFormatWithBsonReferenceImplementation(){
        // created by MongoDB BSON serializer implementation.
        val reference = "240000000761000000000000000000000000000274000900000053756363657373210000"

        val doc2 = DocumentNode()
        doc2["a"] = ObjectIdNode(ByteArray(12))
        doc2["t"] = TextNode("Success!")

        val bytes2 = BsonSerializer.serializeBsonDocument(doc2)
        expectThat(bytes2.hex()).isEqualTo(reference)
    }

    @ParameterizedTest
    @EnumSource(SizeMarkersWriterSetting::class)
    fun canSerializeObjectIdNode(sizeMarkersWriterSetting: SizeMarkersWriterSetting) {
        val content = ByteArray(12)
        ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(1)
            .putInt(2)
            .putInt(3)
        val bytes = this.serializeSingeNode(ObjectIdNode(value = content), sizeMarkersWriterSetting)
        expectThat(bytes) {
            get { this.size }.isEqualTo(12)
            get { ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(Int.SIZE_BYTES * 0) }.isEqualTo(1)
                get { this.getInt(Int.SIZE_BYTES * 1) }.isEqualTo(2)
                get { this.getInt(Int.SIZE_BYTES * 2) }.isEqualTo(3)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canDeserializeObjectIdNode(trustSizeMarkers: Boolean) {
        val bytes = ByteArray(12)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(1)
            .putInt(2)
            .putInt(3)
        val node = this.deserializeSingleNode(bytes, 0x07)
        expectThat(node).isA<ObjectIdNode>().and {
            get { this.value.size }.isEqualTo(12)
            get { ByteBuffer.wrap(this.value).order(ByteOrder.LITTLE_ENDIAN) }.and {
                get { this.getInt(Int.SIZE_BYTES * 0) }.isEqualTo(1)
                get { this.getInt(Int.SIZE_BYTES * 1) }.isEqualTo(2)
                get { this.getInt(Int.SIZE_BYTES * 2) }.isEqualTo(3)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun canSkipOverObjectIdNode(trustSizeMarkers: Boolean) {
        val content = ByteArray(12)
        ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(1)
            .putInt(2)
            .putInt(3)
        assertCanSkipOverNode(ObjectIdNode(value = content), trustSizeMarkers)
    }

    @Test
    fun canSerializeAndDeserializeTopLevelObjectIdNode() {
        val node = ObjectIdNode(byteArrayOf(1,2,3))

        val bytes = BsonSerializer.serializeBsonNode(node)
        val deserializedNode = BsonDeserializer.deserializeBsonNode(bytes)

        expectThat(deserializedNode).isEqualTo(node)
    }
}