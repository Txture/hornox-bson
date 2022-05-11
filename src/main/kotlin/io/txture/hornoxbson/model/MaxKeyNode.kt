package io.txture.hornoxbson.model

import jakarta.json.JsonValue

object MaxKeyNode : BsonNode {

    @JvmField
    val FINGERPRINT_BYTE = 0x7F.toByte()

    override val fingerprintByte: Byte
        get() = FINGERPRINT_BYTE

    override fun toString(): String {
        return "undefined"
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NULL
    }

}