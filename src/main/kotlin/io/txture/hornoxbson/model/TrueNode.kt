package io.txture.hornoxbson.model

import jakarta.json.JsonValue

object TrueNode : BooleanNode {

    override val value: Boolean
        get() = true

    override fun toString(): String {
        return "true"
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.TRUE
    }

}