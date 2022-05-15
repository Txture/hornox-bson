package io.txture.hornoxbson.test.cases

import io.txture.hornoxbson.ByteExtensions.hex
import io.txture.hornoxbson.util.LittleEndianExtensions.readLittleEndianDouble
import io.txture.hornoxbson.util.LittleEndianExtensions.readLittleEndianLong
import io.txture.hornoxbson.util.LittleEndianExtensions.writeLittleEndianDouble
import io.txture.hornoxbson.util.LittleEndianExtensions.writeLittleEndianLong
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class LittleEndianUtilsTest {

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
        ByteArrayInputStream(byteArray).use { bais ->
            return bais.readLittleEndianDouble()
        }
    }

    private fun byteArrayToLong(byteArray: ByteArray): Long {
        ByteArrayInputStream(byteArray).use { bais ->
            return bais.readLittleEndianLong()
        }
    }

    private fun Long.hex(): String {
        val bytes = ByteArray(8)
        ByteBuffer.wrap(bytes).putLong(this)
        return bytes.hex()
    }

}