package io.txture.hornoxbson.util

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.min

/**
 * Implementation of the [InputStream] interface via a backing [ByteBuffer].
 *
 */
class ByteBufferInputStream(
    val buffer: ByteBuffer
) : InputStream() {

    override fun read(): Int {
        return if (!buffer.hasRemaining()) {
            -1
        } else {
            buffer.get().toInt() and 0xFF
        }
    }

    override fun read(bytes: ByteArray, off: Int, len: Int): Int {
        if (!buffer.hasRemaining()) {
            return -1
        }
        val actualLength = min(len, buffer.remaining())
        buffer[bytes, off, actualLength]
        return actualLength
    }
}