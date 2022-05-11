package io.txture.hornoxbson.model

import jakarta.json.JsonValue

object NullNode : BsonNode {

    @JvmField
    val FINGERPRINT_BYTE = 0x0A.toByte()

    override val fingerprintByte: Byte
        get() = FINGERPRINT_BYTE

    override fun toString(): String {
        return "null"
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NULL
    }

}