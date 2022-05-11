package io.txture.hornoxbson.model

import jakarta.json.JsonNumber
import jakarta.json.JsonValue

sealed interface BsonNumberNode<T: Number>: BsonValueNode<T>, JsonNumber {

    override fun doubleValue(): Double {
        return this.value.toDouble()
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NUMBER
    }

    override fun intValue(): Int {
        return this.value.toInt()
    }

    override fun intValueExact(): Int {
        return this.bigDecimalValue().intValueExact()
    }

    override fun longValue(): Long {
        return this.value.toLong()
    }

    override fun longValueExact(): Long {
        return this.bigDecimalValue().longValueExact()
    }

}