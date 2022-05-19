package io.txture.hornoxbson.model

import jakarta.json.JsonValue

object MaxKeyNode : BsonNode {

    override val nodeType: NodeType
        get() = NodeType.MAX_KEY

    override fun toString(): String {
        return "MaxKey"
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NULL
    }

}