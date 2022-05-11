package io.txture.hornoxbson.model

import jakarta.json.JsonValue

object UndefinedNode : BsonNode {

    @JvmField
    val FINGERPRINT_BYTE = 0x06.toByte()

    override val fingerprintByte: Byte
        get() = FINGERPRINT_BYTE

    override fun toString(): String {
        return "undefined"
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.NULL
    }

}