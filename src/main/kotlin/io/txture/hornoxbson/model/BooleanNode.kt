package io.txture.hornoxbson.model

sealed interface BooleanNode : BsonValueNode<Boolean> {

    companion object {

        @JvmField
        val FINGERPRINT_BYTE = 0x08.toByte()

    }

    override val fingerprintByte: Byte
        get() = FINGERPRINT_BYTE

}