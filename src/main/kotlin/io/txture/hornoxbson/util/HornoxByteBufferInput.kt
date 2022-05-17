package io.txture.hornoxbson.util

import io.txture.hornoxbson.ByteExtensions.NULL_BYTE
import io.txture.hornoxbson.exception.EndOfInputException
import java.nio.ByteBuffer

class HornoxByteBufferInput(
    /** The backing byte buffer.*/
    private val byteBuffer: ByteBuffer,
    /** Whether to trust the length markers of strings.*/
    private val trustStringSizeMarkers: Boolean,
) : HornoxInput {


    override fun readString(): String {
        return if (this.trustStringSizeMarkers) {
            val length = this.readLittleEndianInt()
            if (isValidStringSizeMarker(length)) {
                val result = String(this.byteBuffer.array(), this.byteBuffer.position(), length - 1 /* ignore NULL terminator*/)
                this.byteBuffer.position(this.byteBuffer.position() + length)
                result
            } else {
                // size marker is invalid
                this.readCString()
            }
        } else {
            // we don't trust the size marker. Skip it.
            this.skipBytes(Int.SIZE_BYTES)
            this.readCString()
        }
    }

    private fun isValidStringSizeMarker(length: Int): Boolean {
        if (length <= 0) {
            return false
        }
        val newPosition = this.byteBuffer.position() + length
        if (newPosition >= this.byteBuffer.array().size) {
            // length is too big
            return false
        }
        // the character at the end position must be a null terminator.
        // Please note that this is not a 100% safe check; the null
        // byte may technically belong to some other entry. But if there
        // is NO null terminator at the end position, we know for sure
        // that the size marker isn't trustworthy.
        if (this.byteBuffer.array()[newPosition] != NULL_BYTE) {
            return false
        }
        return true
    }


    override fun skipCString(): HornoxInput {
        while (this.readByte() != NULL_BYTE) {
            // no-op
        }
        return this
    }

    override fun readCString(): String {
        val start = this.byteBuffer.position()
        this.skipCString()
        val length = this.byteBuffer.position() - 1 /* NULL terminator */ - start
        return String(this.byteBuffer.array(), start, length)
    }

    override fun readByte(): Byte {
        if (this.byteBuffer.hasRemaining()) {
            return this.byteBuffer.get()
        } else {
            throw EndOfInputException("Cannot read byte from input, because there are no more bytes to read after index ${this.byteBuffer.position()}!")
        }
    }

    override fun skipBytes(numberOfBytes: Int): HornoxInput {
        require(numberOfBytes >= 0) { "The numberOfBytes to skip must not be negative (given value: ${numberOfBytes})!" }
        val newPosition = this.byteBuffer.position() + numberOfBytes
        val limit = this.byteBuffer.array().size
        if (newPosition >= limit) {
            throw EndOfInputException("Cannot skip ${numberOfBytes} from position ${this.byteBuffer.position()}, as it would exceed the end index (${limit})!")
        }
        this.byteBuffer.position(newPosition)
        return this
    }

    override fun readByteArrayOfLength(length: Int): ByteArray {
        require(length >= 0) { "The length of the byte array to read must not be negative (given value: ${length})!" }
        val newPosition = this.byteBuffer.position() + length
        val limit = this.byteBuffer.array().size
        if (newPosition >= limit) {
            throw EndOfInputException("Cannot skip ${length} from position ${this.byteBuffer.position()}, as it would exceed the end index (${limit})!")
        }
        val array = ByteArray(length)
        System.arraycopy(
            /* src = */ this.byteBuffer.array(),
            /* srcPos = */ this.byteBuffer.position(),
            /* dest = */ array,
            /* destPos = */ 0,
            /* length = */ length
        )
        this.byteBuffer.position(this.byteBuffer.position() + length)
        return array
    }


}