package io.txture.hornoxbson.model

import jakarta.json.JsonString
import jakarta.json.JsonValue

class SymbolNode(
    override val value: String,
) : BsonValueNode<String>, JsonString {

    override val nodeType: NodeType
        get() = NodeType.SYMBOL

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

        other as SymbolNode

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "S\"${this.value}\""
    }
}