package io.txture.hornoxbson.model

import jakarta.json.JsonValue

object MinKeyNode : BsonNode {

    override val nodeType: NodeType
        get() = NodeType.MIN_KEY

    override fun toString(): String {
        return "MinKey"
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NULL
    }

}