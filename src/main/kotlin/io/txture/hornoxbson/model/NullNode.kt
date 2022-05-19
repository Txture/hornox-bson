package io.txture.hornoxbson.model

import jakarta.json.JsonValue

object NullNode : BsonNode {

    override val nodeType: NodeType
        get() = NodeType.NULL

    override fun toString(): String {
        return "null"
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NULL
    }

}