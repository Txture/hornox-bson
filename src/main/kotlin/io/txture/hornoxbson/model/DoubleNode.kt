package io.txture.hornoxbson.model

import jakarta.json.JsonValue
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.floor

class DoubleNode(
    override val value: Double,
) : BsonNumberNode<Double> {

    companion object {

        @JvmField
        val FINGERPRINT_BYTE = 0x01.toByte()

    }

    override val nodeType: NodeType
        get() = NodeType.DOUBLE

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NUMBER
    }

    override fun bigDecimalValue(): BigDecimal {
        return BigDecimal.valueOf(this.value)
    }

    override fun bigIntegerValue(): BigInteger {
        return this.bigDecimalValue().toBigInteger()
    }

    override fun bigIntegerValueExact(): BigInteger {
        return this.bigDecimalValue().toBigIntegerExact()
    }

    override fun isIntegral(): Boolean {
        if ((this.value == floor(this.value)) && this.value.isInfinite()) {
            return true
        }
        return false
    }

    override fun intValue(): Int {
        return this.value.toInt()
    }

    override fun intValueExact(): Int {
        if(this.isIntegral){
            return this.value.toInt()
        }else{
            throw ArithmeticException("Value is not integral - exact conversion is not possible!")
        }
    }

    override fun longValue(): Long {
        return this.value.toLong()
    }

    override fun longValueExact(): Long {
        if(this.isIntegral){
            return this.value.toLong()
        }else{
            throw ArithmeticException("Value is not integral - exact conversion is not possible!")
        }
    }

    override fun doubleValue(): Double {
        return this.value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as DoubleNode

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return this.value.toString()
    }

}