package io.txture.hornoxbson.util

import io.txture.hornoxbson.ByteExtensions.NULL_BYTE
import io.txture.hornoxbson.exception.EndOfInputException
import kotlin.math.min

class HornoxByteArrayInput(
    /** The byte array to scan */
    private val byteArray: ByteArray,
    /** Whether to trust the size markers of strings.*/
    private val trustStringSizeMarker: Boolean,
    /** Start index, inclusive */
    private val startIndex: Int,
    /** End index, exclusive */
    endIndex: Int,
) : HornoxInput {

    private var currentPosition = this.startIndex

    private val endIndex = min(endIndex, byteArray.size)

    override fun readString(): String {
        return if (this.trustStringSizeMarker) {
            val length = this.readLittleEndianInt()
            if (isValidStringSizeMarker(length)) {
                val result = String(this.byteArray, this.currentPosition, length - 1 /* ignore NULL terminator*/)
                this.currentPosition += length
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
        val newPosition = this.currentPosition + length
        if (newPosition >= this.endIndex) {
            // length is too big
            return false
        }
        // the character at the end position must be a null terminator.
        // Please note that this is not a 100% safe check; the null
        // byte may technically belong to some other entry. But if there
        // is NO null terminator at the end position, we know for sure
        // that the size marker isn't trustworthy.
        if (this.byteArray[newPosition] != NULL_BYTE) {
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
        val start = this.currentPosition
        this.skipCString()
        val length = this.currentPosition - 1 /* NULL terminator */ - start
        return String(this.byteArray, start, length)
    }

    override fun readByte(): Byte {
        if (this.currentPosition >= endIndex) {
            throw EndOfInputException("Cannot read byte from input, because ther are no more bytes to read after index ${this.currentPosition}!")
        }
        val byte = this.byteArray[currentPosition]
        this.currentPosition++
        return byte
    }

    override fun skipBytes(numberOfBytes: Int): HornoxInput {
        require(numberOfBytes >= 0) { "The numberOfBytes to skip must not be negative (given value: ${numberOfBytes})!" }
        val newPosition = this.currentPosition + numberOfBytes
        if (newPosition >= this.endIndex) {
            throw EndOfInputException("Cannot skip ${numberOfBytes} from position ${this.currentPosition}, as it would exceed the end index (${this.endIndex})!")
        }
        this.currentPosition = newPosition
        return this
    }

    override fun readByteArrayOfLength(length: Int): ByteArray {
        require(length >= 0) { "The length of the byte array to read must not be negative (given value: ${length})!" }
        val newPosition = this.currentPosition + length
        if (newPosition >= this.endIndex) {
            throw EndOfInputException("Cannot skip ${length} from position ${this.currentPosition}, as it would exceed the end index (${this.endIndex})!")
        }
        val array = ByteArray(length)
        System.arraycopy(
            /* src = */ this.byteArray,
            /* srcPos = */ this.currentPosition,
            /* dest = */ array,
            /* destPos = */ 0,
            /* length = */ length
        )
        this.currentPosition += length
        return array
    }


}