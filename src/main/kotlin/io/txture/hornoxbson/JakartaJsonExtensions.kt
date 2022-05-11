package io.txture.hornoxbson

import jakarta.json.*
import io.txture.hornoxbson.model.*

object JakartaJsonExtensions {

    fun JsonValue?.toBsonNode(): BsonNode {
        when {
            this is BsonNode -> return this // already a Bson node, no need to convert
            this == null || this.valueType == JsonValue.ValueType.NULL -> return NullNode
            this is JsonNumber -> return DoubleNode(this.doubleValue())
            this is JsonString -> return TextNode(this.string)
            this is JsonObject -> return DocumentNode().also {
                for ((key, value) in this.entries) {
                    it.fields[key] = value.toBsonNode()
                }
            }
            this is JsonArray -> return ArrayNode().also {
                for(element in this){
                    it.fields.add(element.toBsonNode())
                }
            }
            this.valueType == JsonValue.ValueType.TRUE -> return TrueNode
            this.valueType == JsonValue.ValueType.FALSE -> return FalseNode
            else -> throw IllegalArgumentException("No conversion from ${this.javaClass.name} to BsonNode found!")
        }
    }

}