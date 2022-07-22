package io.txture.hornoxbson.model

import jakarta.json.JsonValue
import java.math.BigDecimal
import java.math.BigInteger

class Int32Node(
    override val value: Int,
) : BsonNumberNode<Int> {

    override val nodeType: NodeType
        get() = NodeType.INT32

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NUMBER
    }

    override fun bigDecimalValue(): BigDecimal {
        return BigDecimal.valueOf(this.value.toDouble())
    }

    override fun bigIntegerValue(): BigInteger {
        return BigInteger.valueOf(this.value.toLong())
    }

    override fun bigIntegerValueExact(): BigInteger {
        return BigInteger.valueOf(this.value.toLong())
    }

    override fun isIntegral(): Boolean {
        return true
    }

    override fun intValue(): Int {
        return this.value
    }

    override fun intValueExact(): Int {
        return this.value
    }

    override fun longValue(): Long {
        return this.value.toLong()
    }

    override fun longValueExact(): Long {
        return this.value.toLong()
    }

    override fun doubleValue(): Double {
        return this.value.toDouble()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Int32Node

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value
        return result
    }

    override fun toString(): String {
        return this.value.toString()
    }


}