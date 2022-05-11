package io.txture.hornoxbson.model

enum class BinarySubtype(
    val byte: Byte,
) {
    GENERIC(0x00.toByte()),
    FUNCTION(0x01.toByte()),
    UUID(0x04.toByte()),
    MD5(0x05.toByte()),
    ENCRYPTED(0x06.toByte()),
    COMPRESSED(0x07.toByte()),
    USER_DEFINED(0x80.toByte());

    companion object {

        fun fromByte(byte: Byte): BinarySubtype {
            for (literal in values()) {
                if (literal.byte == byte) {
                    return literal
                }
            }
            throw IllegalArgumentException("The given byte does not correspond to any known binary subtype: ${byte}")
        }

    }
}