package io.txture.hornoxbson

import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting.*
import io.txture.hornoxbson.ByteExtensions.NULL_BYTE
import io.txture.hornoxbson.model.*
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Serializer / Writer for BSON.
 *
 * Converts [DocumentNode]s into [ByteArray]s or fills [OutputStream]s.
 */
object BsonSerializer {

    /**
     * Serializes the given [documentNode] into a [ByteArray].
     *
     * @param documentNode The document to serialize.
     * @param sizeMarkers Indicates which size markers to write into the output array. Please refer to the documentation
     * of the individual literals for details. The default is [RECOMPUTE].
     *
     * @return A byte array in BSON-format that contains the given document.
     */
    @JvmStatic
    @JvmOverloads
    fun serializeBsonDocument(documentNode: DocumentNode, sizeMarkers: SizeMarkersWriterSetting = RECOMPUTE): ByteArray {
        ByteArrayOutputStream().use { baos ->
            serializeBsonDocument(documentNode, baos, sizeMarkers)
            return baos.toByteArray()
        }
    }

    /**
     * Serializes the given [documentNode] into the given [outputStream].
     *
     * @param documentNode The document to serialize.
     * @param outputStream The output stream to write the data into. The caller is responsible for providing an open stream, and is also responsible for closing it.
     * @param sizeMarkers Indicates which size markers to write into the output array. Please refer to the documentation
     * of the individual literals for details. The default is [RECOMPUTE].
     */
    @JvmStatic
    fun serializeBsonDocument(documentNode: DocumentNode, outputStream: OutputStream, sizeMarkers: SizeMarkersWriterSetting) {
        when (sizeMarkers) {
            WRITE_MINUS_1 -> {
                outputStream.writeInt(-1)
                for ((fieldName, valueNode) in documentNode.fields) {
                    outputStream.writeByte(valueNode.fingerprintByte)
                    outputStream.writeCString(fieldName)
                    writeFieldValue(valueNode, outputStream, sizeMarkers)
                }
                outputStream.writeNullByte() // end of document
            }
            TRUST_DOCUMENT -> {
                outputStream.writeInt(documentNode.length)
                for ((fieldName, valueNode) in documentNode.fields) {
                    outputStream.writeByte(valueNode.fingerprintByte)
                    outputStream.writeCString(fieldName)
                    writeFieldValue(valueNode, outputStream, sizeMarkers)
                }
                outputStream.writeNullByte() // end of document
            }
            RECOMPUTE -> {
                val docBytes = ByteArrayOutputStream().use { innerStream ->
                    for ((fieldName, valueNode) in documentNode.fields) {
                        innerStream.writeByte(valueNode.fingerprintByte)
                        innerStream.writeCString(fieldName)
                        writeFieldValue(valueNode, innerStream, sizeMarkers)
                    }
                    innerStream.toByteArray()
                }
                // size is the overall size of the content byte array
                //  + the length of the size integer itself
                //  + the terminating null byte
                val newSize = docBytes.size + Int.SIZE_BYTES + 1
                documentNode.length = newSize
                outputStream.writeInt(newSize)
                outputStream.write(docBytes)
                outputStream.writeNullByte()
            }
        }

    }

    private fun writeBsonArray(arrayNode: ArrayNode, out: OutputStream, sizeMarkers: SizeMarkersWriterSetting) {
        when (sizeMarkers) {
            WRITE_MINUS_1 -> {
                out.writeInt(-1)
                for ((index, field) in arrayNode.fields.withIndex()) {
                    out.writeByte(field.fingerprintByte)
                    out.writeCString(index.toString())
                    writeFieldValue(field, out, sizeMarkers)
                }
                out.writeNullByte() // end of array according to BSON spec
            }
            TRUST_DOCUMENT -> {
                out.writeInt(arrayNode.length)
                for ((index, field) in arrayNode.fields.withIndex()) {
                    out.writeByte(field.fingerprintByte)
                    out.writeCString(index.toString())
                    writeFieldValue(field, out, sizeMarkers)
                }
                out.writeNullByte() // end of array according to BSON spec
            }
            RECOMPUTE -> {
                val entryBytes = ByteArrayOutputStream().use { innerStream ->
                    for ((index, field) in arrayNode.fields.withIndex()) {
                        innerStream.writeByte(field.fingerprintByte)
                        innerStream.writeCString(index.toString())
                        writeFieldValue(field, innerStream, sizeMarkers)
                    }
                    innerStream.toByteArray()
                }
                // size is the overall size of the content byte array
                //  + the length of the size integer itself
                //  + the terminating null byte
                val newSize = entryBytes.size + Int.SIZE_BYTES + 1
                arrayNode.length = newSize
                out.writeInt(newSize)
                out.write(entryBytes)
                out.writeNullByte() // end of array according to BSON spec
            }
        }
    }

    private fun writeJavaScriptWithScopeValue(node: JavaScriptWithScopeNode, out: OutputStream, sizeMarkers: SizeMarkersWriterSetting) {
        when (sizeMarkers) {
            WRITE_MINUS_1 -> {
                out.writeInt(-1)
                out.writeString(node.value)
                serializeBsonDocument(node.scope, out, sizeMarkers)
            }
            TRUST_DOCUMENT -> {
                val codeBytes = node.string.toByteArray()
                val totalSize = node.scope.length + // context document length
                    Int.SIZE_BYTES + // size of the code_w_s node
                    codeBytes.size + // size of the source code
                    Int.SIZE_BYTES + // length of the source code
                    1 // null-terminator of the source code
                out.writeInt(totalSize)
                out.writeInt(codeBytes.size + 1)
                out.write(codeBytes)
                out.writeNullByte()
                serializeBsonDocument(node.scope, out, sizeMarkers)
            }
            RECOMPUTE -> {
                val codeBytes = node.string.toByteArray()
                val scopeContentBytes = ByteArrayOutputStream().use { innerStream ->
                    for ((fieldName, valueNode) in node.scope.fields) {
                        innerStream.writeByte(valueNode.fingerprintByte)
                        innerStream.writeCString(fieldName)
                        writeFieldValue(valueNode, innerStream, sizeMarkers)
                    }
                    innerStream.toByteArray()
                }
                val totalSize = Int.SIZE_BYTES + // length of whole code_w_s node
                    Int.SIZE_BYTES + // length field of code string
                    codeBytes.size + // code bytes content
                    1 + // null terminator of code string
                    Int.SIZE_BYTES + // length of scope document
                    scopeContentBytes.size + // scope document content
                    1 // null byte that terminates the scope document
                out.writeInt(totalSize)
                out.writeInt(codeBytes.size + 1)
                out.write(codeBytes)
                out.writeNullByte()
                out.writeInt(scopeContentBytes.size + Int.SIZE_BYTES + 1)
                out.write(scopeContentBytes)
                out.writeNullByte()
            }
        }
    }

    private fun writeFieldValue(node: BsonNode, out: OutputStream, sizeMarkers: SizeMarkersWriterSetting) {
        when (node) {
            is DoubleNode -> {
                out.writeDouble(node.value)
            }
            is TextNode -> {
                out.writeString(node.value)
            }
            is DocumentNode -> serializeBsonDocument(node, out, sizeMarkers)
            is ArrayNode -> writeBsonArray(node, out, sizeMarkers)
            is BinaryNode -> {
                out.writeInt(node.value.size)
                out.writeByte(node.subtype.byte)
                out.write(node.value)
            }
            UndefinedNode -> {
                // no-op
            }
            is ObjectIdNode -> {
                out.write(node.value)
            }
            FalseNode -> out.writeByte(NULL_BYTE)
            TrueNode -> out.writeByte(0x01)
            is UtcDateTimeNode -> {
                out.writeLong(node.value)
            }
            NullNode -> {
                // no-op
            }
            is RegularExpressionNode -> {
                out.writeCString(node.regularExpression)
                out.writeCString(node.options)
            }
            is DbPointerNode -> {
                out.writeString(node.name)
                out.write(node.value)
            }
            is JavaScriptNode -> {
                out.writeString(node.value)
            }
            is SymbolNode -> {
                out.writeString(node.value)
            }
            is JavaScriptWithScopeNode -> {
                writeJavaScriptWithScopeValue(node, out, sizeMarkers)
            }
            is Int32Node -> {
                out.writeInt(node.value)
            }
            is TimestampNode -> {
                out.writeLong(node.value)
            }
            is Int64Node -> {
                out.writeLong(node.value)
            }
            is Decimal128Node -> {
                out.write(node.value)
            }
            MaxKeyNode -> {
                // no-op
            }
            MinKeyNode -> {
                // no-op
            }
        }
    }

    private fun intToBytes(value: Int): ByteArray {
        val bb = ByteBuffer.allocate(Int.SIZE_BYTES)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        bb.putInt(value)
        return bb.array()
    }

    private fun longToBytes(value: Long): ByteArray {
        val bb = ByteBuffer.allocate(Long.SIZE_BYTES)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        bb.putLong(value)
        return bb.array()
    }

    private fun doubleToBytes(value: Double): ByteArray {
        val bb = ByteBuffer.allocate(Double.SIZE_BYTES)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        bb.putDouble(value)
        return bb.array()
    }


    private fun OutputStream.writeString(string: String): OutputStream {
        val bytes = string.toByteArray()
        this.writeInt(bytes.size + 1) // +1 for NULL terminator
        this.write(bytes)
        this.writeByte(NULL_BYTE)
        return this
    }

    private fun OutputStream.writeCString(string: String): OutputStream {
        val bytes = string.toByteArray()
        this.write(bytes)
        this.writeByte(NULL_BYTE) // BSON strings are NULL-terminated
        return this
    }

    private fun OutputStream.writeInt(int: Int): OutputStream {
        this.write(intToBytes(int))
        return this
    }

    private fun OutputStream.writeLong(long: Long): OutputStream {
        this.write(longToBytes(long))
        return this
    }

    private fun OutputStream.writeDouble(double: Double): OutputStream {
        this.write(doubleToBytes(double))
        return this
    }

    private fun OutputStream.writeByte(byte: Byte): OutputStream {
        this.write(byte.toInt())
        return this
    }

    private fun OutputStream.writeByte(int: Int): OutputStream {
        this.write(int)
        return this
    }

    private fun OutputStream.writeNullByte(): OutputStream {
        this.writeByte(NULL_BYTE)
        return this
    }

    enum class SizeMarkersWriterSetting {

        /**
         * All "size" fields for array and document nodes in the resulting BSON binary will have a value of -1.
         */
        WRITE_MINUS_1,

        /**
         * The serializer will write the size value which is contained in the document (for array and document nodes).
         *
         * This is generally not recommended, as in-memory modifications to the document do not automatically
         * update the size values. Only use this if you are re-serializing a known (unmodified) document that is
         * known to have valid sizes.
         */
        TRUST_DOCUMENT,

        /**
         * Recomputes the "size" field(s) in the document
         */
        RECOMPUTE

    }
}