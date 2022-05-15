package io.txture.hornoxbson.util

import java.io.InputStream
import java.io.OutputStream

object LittleEndianExtensions {

    @JvmStatic
    fun OutputStream.writeLittleEndianInt(int: Int): OutputStream {
        // The strategy here is as follows:
        // - shift the bytes of the long such that the relevant byte is at the LAST (rightmost position)
        // - then apply a bitmask that eliminates all bits except for the last 8
        // - write that into the output, then continue with the next byte in the same fashion.
        this.write((int.ushr(0*8) and 0xFF))
        this.write((int.ushr(1*8) and 0xFF))
        this.write((int.ushr(2*8) and 0xFF))
        this.write((int.ushr(3*8) and 0xFF))
        return this
    }

    @JvmStatic
    fun OutputStream.writeLittleEndianLong(long: Long): OutputStream {
        // The strategy here is as follows:
        // - shift the bytes of the long such that the relevant byte is at the LAST (rightmost position)
        // - then apply a bitmask that eliminates all bits except for the last 8
        // - write that into the output, then continue with the next byte in the same fashion.
        this.write((long.ushr(0*8) and 0xFF).toInt())
        this.write((long.ushr(1*8) and 0xFF).toInt())
        this.write((long.ushr(2*8) and 0xFF).toInt())
        this.write((long.ushr(3*8) and 0xFF).toInt())
        this.write((long.ushr(4*8) and 0xFF).toInt())
        this.write((long.ushr(5*8) and 0xFF).toInt())
        this.write((long.ushr(6*8) and 0xFF).toInt())
        this.write((long.ushr(7*8) and 0xFF).toInt())
        return this
    }

    @JvmStatic
    fun OutputStream.writeLittleEndianDouble(double: Double): OutputStream {
        return this.writeLittleEndianLong(double.toBits())
    }

    @JvmStatic
    fun InputStream.readLittleEndianLong(): Long {
        // The strategy here is as follows:
        // - we read a single byte from the input stream
        // - then we mask out everything except for the last byte
        //   (this avoids the case where a negative number would have a 1-bit somewhere except in the last byte)
        // - shift the byte to the appropriate position
        // - apply a bit-wise OR to everything to arrive at the final result
        return this.read().toLong() or
            (this.read().toLong() and 0x000000FF).shl(1 * 8) or
            (this.read().toLong() and 0x000000FF).shl(2 * 8) or
            (this.read().toLong() and 0x000000FF).shl(3 * 8) or
            (this.read().toLong() and 0x000000FF).shl(4 * 8) or
            (this.read().toLong() and 0x000000FF).shl(5 * 8) or
            (this.read().toLong() and 0x000000FF).shl(6 * 8) or
            (this.read().toLong() and 0x000000FF).shl(7 * 8)
    }

    @JvmStatic
    fun InputStream.readLittleEndianDouble(): Double {
        return Double.fromBits(this.readLittleEndianLong())
    }

    @JvmStatic
    fun InputStream.readLittleEndianInt(): Int {
        // The strategy here is as follows:
        // - we read a single byte from the input stream
        // - then we mask out everything except for the last byte
        //   (this avoids the case where a negative number would have a 1-bit somewhere except in the last byte)
        // - shift the byte to the appropriate position
        // - apply a bit-wise OR to everything to arrive at the final result
        return this.read() or
            (this.read() and 0x000000FF).shl(1 * 8) or
            (this.read() and 0x000000FF).shl(2 * 8) or
            (this.read() and 0x000000FF).shl(3 * 8)
    }

}