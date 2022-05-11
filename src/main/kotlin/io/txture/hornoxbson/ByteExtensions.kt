package io.txture.hornoxbson

import java.util.*

object ByteExtensions {

    @JvmStatic
    val NULL_BYTE = 0x00.toByte()

    fun ByteArray.hex(): String {
        return HexFormat.of().formatHex(this)
    }

    fun Byte.hex(): String {
        return HexFormat.of().toHexDigits(this)
    }

}