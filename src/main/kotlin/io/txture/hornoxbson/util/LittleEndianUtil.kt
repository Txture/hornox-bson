package io.txture.hornoxbson.util

object LittleEndianUtil {


    @Suppress("NOTHING_TO_INLINE")
    inline fun readLittleEndianLong(
        b0: Byte,
        b1: Byte,
        b2: Byte,
        b3: Byte,
        b4: Byte,
        b5: Byte,
        b6: Byte,
        b7: Byte,
    ): Long {
        return (b0.toLong() and 0x000000FF) or
            (b1.toLong() and 0x000000FF).shl(1 * 8) or
            (b2.toLong() and 0x000000FF).shl(2 * 8) or
            (b3.toLong() and 0x000000FF).shl(3 * 8) or
            (b4.toLong() and 0x000000FF).shl(4 * 8) or
            (b5.toLong() and 0x000000FF).shl(5 * 8) or
            (b6.toLong() and 0x000000FF).shl(6 * 8) or
            (b7.toLong() and 0x000000FF).shl(7 * 8)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun readLittleEndianDouble(
        b0: Byte,
        b1: Byte,
        b2: Byte,
        b3: Byte,
        b4: Byte,
        b5: Byte,
        b6: Byte,
        b7: Byte,
    ): Double {
        return Double.fromBits(this.readLittleEndianLong(b0, b1, b2, b3, b4, b5, b6, b7))
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun readLittleEndianInt(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {
        return (b0.toInt()  and 0x000000FF) or
            (b1.toInt() and 0x000000FF).shl(1 * 8) or
            (b2.toInt() and 0x000000FF).shl(2 * 8) or
            (b3.toInt() and 0x000000FF).shl(3 * 8)
    }

}