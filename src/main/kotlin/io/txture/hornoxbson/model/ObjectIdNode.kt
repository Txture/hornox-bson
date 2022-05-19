package io.txture.hornoxbson.model

import jakarta.json.JsonString
import jakarta.json.JsonValue
import io.txture.hornoxbson.ByteExtensions.hex

class ObjectIdNode : BsonValueNode<ByteArray>, JsonString {

    companion object {

        val SIZE_BYTES = 12 // according to BSON spec, a DBPointer has exactly 12 bytes.

    }

    override val nodeType: NodeType
        get() = NodeType.OBJECT_ID

    override val value: ByteArray

    constructor(value: ByteArray){
        require(value.size <= SIZE_BYTES){
            "The given byte array has length ${value.size}. ObjectIdNodes can only hold up to ${SIZE_BYTES} bytes."
        }
        val newBytes = ByteArray(12)
        if(value.size < SIZE_BYTES){
            // write the bytes we have in front...
            for(index in value.indices){
                newBytes[index] = value[index]
            }
            // ... and fill the rest with zero
            for(index in value.size until SIZE_BYTES){
                newBytes[index] = 0
            }
        }else{
            System.arraycopy(value, 0, newBytes, 0, SIZE_BYTES)
        }
        this.value = newBytes
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.STRING
    }

    override fun getString(): String {
        return this.value.hex()
    }

    override fun getChars(): CharSequence {
        return this.string
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjectIdNode

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun toString(): String {
        return "ID[${this.value.hex()}]"
    }

}