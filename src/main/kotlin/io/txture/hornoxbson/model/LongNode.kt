package io.txture.hornoxbson.model

import jakarta.json.JsonValue
import java.math.BigDecimal
import java.math.BigInteger

sealed interface LongNode: BsonNumberNode<Long> {

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NUMBER
    }

    override fun bigDecimalValue(): BigDecimal {
        return BigDecimal.valueOf(this.value.toDouble())
    }

    override fun bigIntegerValue(): BigInteger {
        return BigInteger.valueOf(this.value)
    }

    override fun bigIntegerValueExact(): BigInteger {
        return BigInteger.valueOf(this.value)
    }

    override fun isIntegral(): Boolean {
        return true
    }

    override fun intValue(): Int {
        return this.value.toInt()
    }

    override fun intValueExact(): Int {
        if(this.value !in Int.MIN_VALUE .. Int.MAX_VALUE){
            throw ArithmeticException("Long value is out-of-bounds for Int data type!")
        }
        return this.value.toInt()
    }

    override fun longValue(): Long {
        return this.value
    }

    override fun longValueExact(): Long {
        return this.value
    }

    override fun doubleValue(): Double {
        return this.value.toDouble()
    }

}