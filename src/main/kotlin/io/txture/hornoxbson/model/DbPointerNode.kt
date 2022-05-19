package io.txture.hornoxbson.model

import jakarta.json.JsonString
import jakarta.json.JsonValue
import io.txture.hornoxbson.ByteExtensions.hex

class DbPointerNode : BsonNode, JsonString {

    companion object {

        val BINARY_PART_SIZE_BYTES = 12 // according to BSON spec, a DBPointer has exactly 12 bytes.

    }

    override val nodeType: NodeType
        get() = NodeType.DB_POINTER

    var name: String

    var value: ByteArray
        set(newValue) {
            require(newValue.size <= BINARY_PART_SIZE_BYTES){
                "A DB-Pointer node can hold up to ${BINARY_PART_SIZE_BYTES} bytes in its value. The given value has ${newValue.size} bytes."
            }
            val newBytes = ByteArray(BINARY_PART_SIZE_BYTES)
            if(newValue.size < BINARY_PART_SIZE_BYTES){
                // write the bytes we have in front...
                for(index in newValue.indices){
                    newBytes[index] = newValue[index]
                }
                // ... and fill the rest with zero
                for(index in newValue.size until BINARY_PART_SIZE_BYTES){
                    newBytes[index] = 0
                }
            }else{
                System.arraycopy(newValue, 0, newBytes, 0, BINARY_PART_SIZE_BYTES)
            }
            field = newBytes
        }

    constructor(name: String, value: ByteArray){
        this.name = name
        this.value = value
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

        other as DbPointerNode

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun toString(): String {
        return "DB[${this.name}: ${this.value.hex()}]"
    }

}