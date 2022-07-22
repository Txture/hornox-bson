package io.txture.hornoxbson.model

import jakarta.json.JsonString
import jakarta.json.JsonValue

class TextNode(
    override val value: String,
) : BsonValueNode<String>, JsonString {

    init {
        require(this.value.none { it.code == 0x00 }){
            "NULL bytes are not allowed in BSON text nodes! Offending value: ${this.value}"
        }
    }

    override val nodeType: NodeType
        get() = NodeType.TEXT

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.STRING
    }

    override fun getChars(): CharSequence {
        return this.value
    }

    override fun getString(): String {
        return this.value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextNode

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "\"${this.value}\""
    }

}