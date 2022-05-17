package io.txture.hornoxbson.util

import java.io.InputStream
import java.nio.ByteBuffer

interface HornoxInput {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun fromByteArray(byteArray: ByteArray, trustStringSizeMarkers: Boolean = false, startIndex: Int = 0, endIndex: Int = byteArray.size): HornoxInput {
            require(startIndex >= 0){ "'startIndex' must  not be negative (given value: ${startIndex})" }
            require(endIndex > startIndex){ "'endIndex' (given value: ${endIndex}) must be greater than 'startIndex' (given value: ${startIndex})" }
            return HornoxByteArrayInput(byteArray, trustStringSizeMarkers, startIndex, endIndex)
        }

        @JvmStatic
        fun fromInputStream(inputStream: InputStream): HornoxInput {
            return HornoxStreamInput(inputStream)
        }

        @JvmStatic
        @JvmOverloads
        fun fromByteBuffer(byteBuffer: ByteBuffer, trustStringSizeMarkers: Boolean = false): HornoxInput {
            return if (byteBuffer.hasArray()) {
                // we have access to the underlying array; use it.
                HornoxByteBufferInput(byteBuffer, trustStringSizeMarkers)
            } else {
                // no underlying array; treat it like a stream.
                fromInputStream(ByteBufferInputStream(byteBuffer))
            }
        }

    }

    fun readString(): String

    fun skipCString(): HornoxInput

    fun readCString(): String

    fun readByte(): Byte

    fun skipBytes(numberOfBytes: Int): HornoxInput

    fun readByteArrayOfLength(length: Int): ByteArray

    fun readLittleEndianDouble(): Double {
        return LittleEndianUtil.readLittleEndianDouble(
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
    }

    fun readLittleEndianInt(): Int {
        return LittleEndianUtil.readLittleEndianInt(
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
    }

    fun readLittleEndianLong(): Long {
        return LittleEndianUtil.readLittleEndianLong(
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
    }

}