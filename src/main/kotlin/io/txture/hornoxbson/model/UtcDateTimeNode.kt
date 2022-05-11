package io.txture.hornoxbson.model

class UtcDateTimeNode(
    override val value: Long,
) : LongNode {

    companion object {

        @JvmField
        val FINGERPRINT_BYTE = 0x09.toByte()

    }

    override val fingerprintByte: Byte
        get() = FINGERPRINT_BYTE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UtcDateTimeNode

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return this.value.toString() + "L"
    }

}