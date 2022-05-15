package io.txture.hornoxbson.util

import java.io.InputStream

object InputStreamExtensions {

    @JvmStatic
    fun InputStream.readByte(): Byte {
        return this.read().toByte()
    }

}