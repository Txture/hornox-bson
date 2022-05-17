package io.txture.hornoxbson.util

import io.txture.hornoxbson.ByteExtensions
import io.txture.hornoxbson.util.InputStreamExtensions.readByte
import java.io.InputStream

class HornoxStreamInput(
    /** The backing input stream.*/
    private val inputStream: InputStream,
) : HornoxInput {

    override fun readString(): String {
        // since we're restricted to the API of an input stream,
        // we have no benefit from the string size marker. Ignore it.
        this.skipBytes(Int.SIZE_BYTES)
        // read the string normally
        return this.readCString()
    }

    override fun skipCString(): HornoxInput {
        while (this.readByte() != ByteExtensions.NULL_BYTE) {
            // no-op
        }
        return this
    }

    override fun readCString(): String {
        val buffer = mutableListOf<Byte>()
        var byte = this.readByte()
        while (byte != ByteExtensions.NULL_BYTE) {
            buffer += byte
            byte = this.readByte()
        }
        return String(buffer.toByteArray())
    }

    override fun readByte(): Byte {
        return this.inputStream.readByte()
    }

    override fun skipBytes(numberOfBytes: Int): HornoxInput {
        repeat(numberOfBytes) {
            this.inputStream.readByte()
        }
        return this
    }

    override fun readByteArrayOfLength(length: Int): ByteArray {
        val array = ByteArray(length)
        repeat(length) { i ->
            array[i] = this.readByte()
        }
        return array
    }

}