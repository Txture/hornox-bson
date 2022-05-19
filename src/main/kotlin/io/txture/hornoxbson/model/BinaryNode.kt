package io.txture.hornoxbson.model

import jakarta.json.JsonString
import jakarta.json.JsonValue
import io.txture.hornoxbson.ByteExtensions.hex

class BinaryNode(
    override val value: ByteArray,
    val subtype: BinarySubtype,
) : BsonValueNode<ByteArray>, JsonString {

    companion object {

        @JvmField
        val FINGERPRINT_BYTE = 0x05.toByte()

    }

    override val fingerprintByte: Byte
        get() = FINGERPRINT_BYTE

    override val nodeType: NodeType
        get() = NodeType.BINARY

    override fun getValueType(): JsonValue.ValueType {
        // there is no "real" value type we can use in the JSON spec,
        // but string comes closest.
        return JsonValue.ValueType.STRING
    }

    override fun getString(): String {
        return this.value.hex()
    }

    override fun getChars(): CharSequence {
        return this.string
    }

    override fun toString(): String {
        return "B[${subtype}, ${value.size} bytes]"
    }

}