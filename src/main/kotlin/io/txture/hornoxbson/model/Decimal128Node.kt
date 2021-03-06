package io.txture.hornoxbson.model

import io.txture.hornoxbson.ByteExtensions.hex
import jakarta.json.JsonString
import jakarta.json.JsonValue

class Decimal128Node : BsonValueNode<ByteArray>, JsonString {

    companion object {

        @JvmField
        val SIZE_BYTES = 128 / 8

    }

    override val nodeType: NodeType
        get() = NodeType.DECIMAL_128

    override val value: ByteArray

    constructor(bytes: ByteArray) {
        require(bytes.size <= SIZE_BYTES){
            "A Decimal-128 node can hold up to ${SIZE_BYTES} bytes. The given value has ${bytes.size} bytes."
        }
        val newBytes = ByteArray(SIZE_BYTES)
        if (bytes.size < SIZE_BYTES) {
            // write the bytes we have in front...
            for (index in bytes.indices) {
                newBytes[index] = bytes[index]
            }
            // ... and fill the rest with zero
            for (index in bytes.size until SIZE_BYTES) {
                newBytes[index] = 0
            }
        } else {
            System.arraycopy(bytes, 0, newBytes, 0, SIZE_BYTES)
        }
        this.value = newBytes
    }

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
        return "D128[${this.value.hex()}]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Decimal128Node

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }


}