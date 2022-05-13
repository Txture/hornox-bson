package io.txture.hornoxbson.test.cases

import io.txture.hornoxbson.ByteExtensions.hex
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.system.measureTimeMillis

class ParseTest {

    @Test
    fun test() {
        val hex = "200000000161006f1283c0ca2109400274000900000053756363657373210000"

        val byteArray = HexFormat.of().parseHex(hex)

        measureTimeMillis {
            var sum = 0L
            repeat(100000) {
                sum += ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getInt()
            }
        }.let { println("Byte buffer took: ${it}ms.") }


        measureTimeMillis {
            var sum = 0L
            repeat(100000) {
                sum += byteArray[3].toInt().shl(3 * 8) or
                    byteArray[2].toInt().shl(2 * 8) or
                    byteArray[1].toInt().shl(1 * 8) or
                    byteArray[0].toInt()
            }
        }.let { println("Byte shifting took: ${it}ms.") }
    }

    @Test
    fun testDouble() {
        val hex = "6f1283c0ca210940"
        val byteArray = HexFormat.of().parseHex(hex)

        val expectedBytes = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getDouble()
        val actualBytes = byteArrayToDouble(byteArray)

        expectThat(actualBytes).isEqualTo(expectedBytes)


        var sum1 = 0.0
        measureTimeMillis {
            repeat(100000) {
                sum1 += ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getDouble()
            }
        }.let { println("Byte buffer took: ${it}ms. Sum = ${sum1.toBits().hex()}") }

        var sum2 = 0.0
        measureTimeMillis {
            repeat(100000) {
                sum2 += byteArrayToDouble(byteArray)
            }
        }.let { println("Byte shifting took: ${it}ms. Sum = ${sum2.toBits().hex()}") }

        expectThat(sum2).isEqualTo(sum1, 0.0000001)
    }

    @Test
    fun roundTripDouble(){
        val hex = "6f1283c0ca210940"
        val byteArray = HexFormat.of().parseHex(hex)

        val expectedDouble = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getDouble()
        val actualDouble = byteArrayToDouble(byteArray)

        expectThat(actualDouble).isEqualTo(expectedDouble)

        val rewrittenBytes = ByteArrayOutputStream().use {
            it.writeLittleEndianDouble(actualDouble)
            it.toByteArray().hex()
        }

        expectThat(rewrittenBytes).isEqualTo(hex)
    }

    @Test
    fun roundTripLong(){
        val hex = "6f1283c0ca210940"
        val byteArray = HexFormat.of().parseHex(hex)

        val expectedLong = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getLong()
        val actualLong = byteArrayToLong(byteArray)

        expectThat(actualLong).isEqualTo(expectedLong)

        val rewrittenBytes = ByteArrayOutputStream().use {
            it.writeLittleEndianLong(actualLong)
            it.toByteArray().hex()
        }

        expectThat(rewrittenBytes).isEqualTo(hex)
    }

    private fun byteArrayToDouble(byteArray: ByteArray): Double {
        return Double.fromBits(byteArrayToLong(byteArray))
    }

    private fun byteArrayToLong(byteArray: ByteArray): Long {
        return (byteArray[7].toLong() and 0x000000FF).shl(7 * 8) or
            (byteArray[6].toLong() and 0x000000FF).shl(6 * 8) or
            (byteArray[5].toLong() and 0x000000FF).shl(5 * 8) or
            (byteArray[4].toLong() and 0x000000FF).shl(4 * 8) or
            (byteArray[3].toLong() and 0x000000FF).shl(3 * 8) or
            (byteArray[2].toLong() and 0x000000FF).shl(2 * 8) or
            (byteArray[1].toLong() and 0x000000FF).shl(1 * 8) or
            byteArray[0].toLong()
    }

    private fun InputStream.readLittleEndianLong(): Long {
        return this.read().toLong() or
            (this.read().toLong() and 0x000000FF).shl(1 * 8) or
            (this.read().toLong() and 0x000000FF).shl(2 * 8) or
            (this.read().toLong() and 0x000000FF).shl(3 * 8) or
            (this.read().toLong() and 0x000000FF).shl(4 * 8) or
            (this.read().toLong() and 0x000000FF).shl(5 * 8) or
            (this.read().toLong() and 0x000000FF).shl(6 * 8) or
            (this.read().toLong() and 0x000000FF).shl(7 * 8)
    }

    private fun InputStream.readLittleEndianDouble(): Double {
        return Double.fromBits(this.readLittleEndianLong())
    }

    private fun OutputStream.writeLittleEndianLong(value: Long) {
        this.write((value.ushr(0*8) and 0xFF).toInt())
        this.write((value.ushr(1*8) and 0xFF).toInt())
        this.write((value.ushr(2*8) and 0xFF).toInt())
        this.write((value.ushr(3*8) and 0xFF).toInt())
        this.write((value.ushr(4*8) and 0xFF).toInt())
        this.write((value.ushr(5*8) and 0xFF).toInt())
        this.write((value.ushr(6*8) and 0xFF).toInt())
        this.write((value.ushr(7*8) and 0xFF).toInt())
    }

    private fun OutputStream.writeLittleEndianDouble(value: Double) {
        this.writeLittleEndianLong(value.toBits())
    }

    private fun Long.hex(): String {
        val bytes = ByteArray(8)
        ByteBuffer.wrap(bytes).putLong(this)
        return bytes.hex()
    }

}