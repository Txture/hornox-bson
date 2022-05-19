package io.txture.hornoxbson.model

import jakarta.json.JsonValue

object FalseNode : BooleanNode {

    override val nodeType: NodeType
        get() = NodeType.FALSE

    override val value: Boolean
        get() = false

    override fun toString(): String {
        return "false"
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.FALSE
    }

}