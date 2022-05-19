package io.txture.hornoxbson.model

import jakarta.json.JsonValue

object UndefinedNode : BsonNode {

    override val nodeType: NodeType
        get() = NodeType.UNDEFINED

    override fun toString(): String {
        return "undefined"
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NULL
    }

}