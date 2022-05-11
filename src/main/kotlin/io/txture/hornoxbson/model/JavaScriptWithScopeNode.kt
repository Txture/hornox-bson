package io.txture.hornoxbson.model

import jakarta.json.JsonString
import jakarta.json.JsonValue

class JavaScriptWithScopeNode: BsonValueNode<String>, JsonString {

    companion object {

        @JvmField
        val FINGERPRINT_BYTE = 0x0F.toByte()

    }

    override val value: String
    val scope: DocumentNode

    constructor(scriptContent: String, context: DocumentNode){
        this.value = scriptContent
        this.scope = context
    }

    override val fingerprintByte: Byte
        get() = FINGERPRINT_BYTE


    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.STRING
    }

    override fun getChars(): CharSequence {
        return this.value
    }

    override fun getString(): String {
        return this.value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JavaScriptWithScopeNode

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "JS\"${this.value}\""
    }
}