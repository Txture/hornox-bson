package io.txture.hornoxbson

import io.txture.hornoxbson.BsonSerializer.SizeMarkersWriterSetting.*
import io.txture.hornoxbson.ByteExtensions.NULL_BYTE
import io.txture.hornoxbson.dommodule.BsonDomModule
import io.txture.hornoxbson.dommodule.HornoxDomModule
import io.txture.hornoxbson.model.*
import io.txture.hornoxbson.util.LittleEndianExtensions.writeLittleEndianDouble
import io.txture.hornoxbson.util.LittleEndianExtensions.writeLittleEndianInt
import io.txture.hornoxbson.util.LittleEndianExtensions.writeLittleEndianLong
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Serializer / Writer for BSON.
 *
 * Converts [DocumentNode]s into [ByteArray]s or fills [OutputStream]s.
 */
object BsonSerializer {

    /**
     * Serializes the given [node] into a new [ByteArray].
     *
     * Please note that the result will **not** be a valid BSON document with respect to the specification, because
     * the specification only allows [DocumentNode]s on the top level. Use [BsonDeserializer.deserializeBsonNode] to
     * read it again.
     *
     * @param node The node to serialize.
     * @param sizeMarkers How size markers should be handled during serialization. The default is [RECOMPUTE].
     *
     * @return The byte array containing the serialized data.
     */
    @JvmStatic
    @JvmOverloads
    fun serializeBsonNode(node: BsonNode, sizeMarkers: SizeMarkersWriterSetting = RECOMPUTE): ByteArray {
        return this.serializeBsonNode(node, sizeMarkers, HornoxDomModule)
    }

    /**
     * Serializes the given [node] into a new [ByteArray].
     *
     * Please note that the result will **not** be a valid BSON document with respect to the specification, because
     * the specification only allows [DocumentNode]s on the top level. Use [BsonDeserializer.deserializeBsonNode] to
     * read it again.
     *
     * @param node The node to serialize.
     * @param sizeMarkers How size markers should be handled during serialization. The default is [RECOMPUTE].
     * @param domModule The DOM module to use for the conversion.
     *
     * @return The byte array containing the serialized data.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> serializeBsonNode(node: T, sizeMarkers: SizeMarkersWriterSetting = RECOMPUTE, domModule: BsonDomModule<T>): ByteArray {
        ByteArrayOutputStream().use { outputStream ->
            this.serializeBsonNode(node, outputStream, sizeMarkers, domModule)
            outputStream.flush()
            return outputStream.toByteArray()
        }
    }

    /**
     * Serializes the given [node] into the given [outputStream].
     *
     * Please note that the result will **not** be a valid BSON document with respect to the specification, because
     * the specification only allows [DocumentNode]s on the top level. Use [BsonDeserializer.deserializeBsonNode] to
     * read it again.
     *
     * @param node The node to serialize.
     * @param outputStream The output stream which should receive the serialized data.
     * @param sizeMarkers How size markers should be handled during serialization. The default is [RECOMPUTE].
     */
    @JvmStatic
    @JvmOverloads
    fun serializeBsonNode(node: BsonNode, outputStream: OutputStream, sizeMarkers: SizeMarkersWriterSetting = RECOMPUTE) {
        this.serializeBsonNode(node, outputStream, sizeMarkers, HornoxDomModule)
    }

    /**
     * Serializes the given [node] into the given [outputStream].
     *
     * Please note that the result will **not** be a valid BSON document with respect to the specification, because
     * the specification only allows [DocumentNode]s on the top level. Use [BsonDeserializer.deserializeBsonNode] to
     * read it again.
     *
     * @param node The node to serialize.
     * @param outputStream The output stream which should receive the serialized data.
     * @param sizeMarkers How size markers should be handled during serialization. The default is [RECOMPUTE].
     * @param domModule The DOM module to use for the conversion.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> serializeBsonNode(node: T, outputStream: OutputStream, sizeMarkers: SizeMarkersWriterSetting = RECOMPUTE, domModule: BsonDomModule<T>) {
        outputStream.write(domModule.getNodeType(node).fingerprintByte.toInt())
        this.writeFieldValue(node, outputStream, sizeMarkers, domModule)
    }

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
        return serializeBsonDocument(documentNode, sizeMarkers, HornoxDomModule)
    }

    /**
     * Serializes the given [documentNode] into a [ByteArray].
     *
     * @param documentNode The document to serialize.
     * @param sizeMarkers Indicates which size markers to write into the output array. Please refer to the documentation
     * of the individual literals for details. The default is [RECOMPUTE].
     * @param domModule The DOM module to use for the conversion.
     *
     * @return A byte array in BSON-format that contains the given document.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> serializeBsonDocument(documentNode: T, sizeMarkers: SizeMarkersWriterSetting = RECOMPUTE, domModule: BsonDomModule<T>): ByteArray {
        ByteArrayOutputStream().use { baos ->
            serializeBsonDocument(documentNode, baos, sizeMarkers, domModule)
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
    @JvmOverloads
    fun serializeBsonDocument(documentNode: DocumentNode, outputStream: OutputStream, sizeMarkers: SizeMarkersWriterSetting = RECOMPUTE) {
        return serializeBsonDocument(documentNode, outputStream, sizeMarkers, HornoxDomModule)
    }

    /**
     * Serializes the given [documentNode] into the given [outputStream].
     *
     * @param documentNode The document to serialize.
     * @param outputStream The output stream to write the data into. The caller is responsible for providing an open stream, and is also responsible for closing it.
     * @param sizeMarkers Indicates which size markers to write into the output array. Please refer to the documentation
     * of the individual literals for details. The default is [RECOMPUTE].
     * @param domModule The DOM module to use for the conversion.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> serializeBsonDocument(documentNode: T, outputStream: OutputStream, sizeMarkers: SizeMarkersWriterSetting = RECOMPUTE, domModule: BsonDomModule<T>) {
        when (sizeMarkers) {
            WRITE_MINUS_1 -> {
                outputStream.writeLittleEndianInt(-1)
                for (fieldName in domModule.getDocumentNodeFields(documentNode)) {
                    val valueNode = domModule.getDocumentNodeFieldValue(documentNode, fieldName)
                        ?: continue
                    val nodeType = domModule.getNodeType(valueNode)
                    outputStream.writeByte(nodeType.fingerprintByte)
                    outputStream.writeCString(fieldName)
                    writeFieldValue(valueNode, outputStream, sizeMarkers, domModule)
                }
                outputStream.writeNullByte() // end of document
            }
            TRUST_DOCUMENT -> {
                val documentBinarySize = domModule.getDocumentNodeSizeBytes(documentNode)
                outputStream.writeLittleEndianInt(documentBinarySize)
                for (fieldName in domModule.getDocumentNodeFields(documentNode)) {
                    val valueNode = domModule.getDocumentNodeFieldValue(documentNode, fieldName)
                        ?: continue
                    val fieldNodeType = domModule.getNodeType(valueNode)
                    outputStream.writeByte(fieldNodeType.fingerprintByte)
                    outputStream.writeCString(fieldName)
                    writeFieldValue(valueNode, outputStream, sizeMarkers, domModule)
                }
                outputStream.writeNullByte() // end of document
            }
            RECOMPUTE -> {
                val docBytes = ByteArrayOutputStream().use { innerStream ->
                    for (fieldName in domModule.getDocumentNodeFields(documentNode)) {
                        val valueNode = domModule.getDocumentNodeFieldValue(documentNode, fieldName)
                            ?: continue
                        val fieldNodeType = domModule.getNodeType(valueNode)
                        innerStream.writeByte(fieldNodeType.fingerprintByte)
                        innerStream.writeCString(fieldName)
                        writeFieldValue(valueNode, innerStream, sizeMarkers, domModule)
                    }
                    innerStream.toByteArray()
                }
                // size is the overall size of the content byte array
                //  + the length of the size integer itself
                //  + the terminating null byte
                val newSize = docBytes.size + Int.SIZE_BYTES + 1
                domModule.setDocumentNodeSizeBytes(documentNode, newSize)
                outputStream.writeLittleEndianInt(newSize)
                outputStream.write(docBytes)
                outputStream.writeNullByte()
            }
        }
    }

    private fun <T> writeBsonArray(arrayNode: T, out: OutputStream, sizeMarkers: SizeMarkersWriterSetting, domModule: BsonDomModule<T>) {
        when (sizeMarkers) {
            WRITE_MINUS_1 -> {
                out.writeLittleEndianInt(-1)
                for (index in 0 until domModule.getArrayNodeLength(arrayNode)) {
                    val entry = domModule.getArrayNodeEntry(arrayNode, index)
                    val nodeType = domModule.getNodeType(entry)
                    out.writeByte(nodeType.fingerprintByte)
                    out.writeCString(index.toString())
                    writeFieldValue(entry, out, sizeMarkers, domModule)
                }
                out.writeNullByte() // end of array according to BSON spec
            }
            TRUST_DOCUMENT -> {
                val length = domModule.getArrayNodeSizeBytes(arrayNode)
                out.writeLittleEndianInt(length)
                for (index in 0 until domModule.getArrayNodeLength(arrayNode)) {
                    val entry = domModule.getArrayNodeEntry(arrayNode, index)
                    val nodeType = domModule.getNodeType(entry)
                    out.writeByte(nodeType.fingerprintByte)
                    out.writeCString(index.toString())
                    writeFieldValue(entry, out, sizeMarkers, domModule)
                }
                out.writeNullByte() // end of array according to BSON spec
            }
            RECOMPUTE -> {
                val entryBytes = ByteArrayOutputStream().use { innerStream ->
                    for (index in 0 until domModule.getArrayNodeLength(arrayNode)) {
                        val entry = domModule.getArrayNodeEntry(arrayNode, index)
                        val nodeType = domModule.getNodeType(entry)
                        innerStream.writeByte(nodeType.fingerprintByte)
                        innerStream.writeCString(index.toString())
                        writeFieldValue(entry, innerStream, sizeMarkers, domModule)
                    }
                    innerStream.toByteArray()
                }
                // size is the overall size of the content byte array
                //  + the length of the size integer itself
                //  + the terminating null byte
                val newSize = entryBytes.size + Int.SIZE_BYTES + 1
                domModule.setArrayNodeSizeBytes(arrayNode, newSize)
                out.writeLittleEndianInt(newSize)
                out.write(entryBytes)
                out.writeNullByte() // end of array according to BSON spec
            }
        }
    }

    private fun <T> writeJavaScriptWithScopeValue(node: T, out: OutputStream, sizeMarkers: SizeMarkersWriterSetting, domModule: BsonDomModule<T>) {
        when (sizeMarkers) {
            WRITE_MINUS_1 -> {
                out.writeLittleEndianInt(-1)
                val scriptContent = domModule.getJavaScriptWithScopeScriptContent(node)
                out.writeString(scriptContent)
                val scopeDocument = domModule.getJavaScriptWithScopeScopeDocument(node)
                serializeBsonDocument(scopeDocument, out, sizeMarkers, domModule)
            }
            TRUST_DOCUMENT -> {
                val scriptContent = domModule.getJavaScriptWithScopeScriptContent(node)
                val scopeDocument = domModule.getJavaScriptWithScopeScopeDocument(node)
                val scopeDocumentLength = domModule.getDocumentNodeSizeBytes(scopeDocument)
                val codeBytes = scriptContent.toByteArray()
                val totalSize = scopeDocumentLength + // context document length
                    Int.SIZE_BYTES + // size of the code_w_s node
                    codeBytes.size + // size of the source code
                    Int.SIZE_BYTES + // length of the source code
                    1 // null-terminator of the source code
                out.writeLittleEndianInt(totalSize)
                out.writeLittleEndianInt(codeBytes.size + 1)
                out.write(codeBytes)
                out.writeNullByte()
                serializeBsonDocument(scopeDocument, out, sizeMarkers, domModule)
            }
            RECOMPUTE -> {
                val scriptContent = domModule.getJavaScriptWithScopeScriptContent(node)
                val scopeDocument = domModule.getJavaScriptWithScopeScopeDocument(node)
                val codeBytes = scriptContent.toByteArray()
                val scopeContentBytes = ByteArrayOutputStream().use { innerStream ->
                    val fieldNames = domModule.getDocumentNodeFields(scopeDocument)
                    for (fieldName in fieldNames) {
                        val valueNode = domModule.getDocumentNodeFieldValue(scopeDocument, fieldName)
                            ?: continue
                        val nodeType = domModule.getNodeType(valueNode)
                        innerStream.writeByte(nodeType.fingerprintByte)
                        innerStream.writeCString(fieldName)
                        writeFieldValue(valueNode, innerStream, sizeMarkers, domModule)
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
                out.writeLittleEndianInt(totalSize)
                out.writeLittleEndianInt(codeBytes.size + 1)
                out.write(codeBytes)
                out.writeNullByte()
                out.writeLittleEndianInt(scopeContentBytes.size + Int.SIZE_BYTES + 1)
                out.write(scopeContentBytes)
                out.writeNullByte()
            }
        }
    }

    private fun <T> writeFieldValue(node: T, out: OutputStream, sizeMarkers: SizeMarkersWriterSetting, domModule: BsonDomModule<T>) {
        when (domModule.getNodeType(node)) {
            NodeType.DOUBLE -> {
                val value = domModule.getDoubleNodeValue(node)
                out.writeLittleEndianDouble(value)
            }
            NodeType.TEXT -> {
                val value = domModule.getTextNodeStringContent(node)
                out.writeString(value)
            }
            NodeType.DOCUMENT -> serializeBsonDocument(node, out, sizeMarkers, domModule)
            NodeType.ARRAY -> writeBsonArray(node, out, sizeMarkers, domModule)
            NodeType.BINARY -> {
                val array = domModule.getBinaryNodeValue(node)
                out.writeLittleEndianInt(array.size)
                val subType = domModule.getBinaryNodeSubType(node)
                out.writeByte(subType.byte)
                out.write(array)
            }
            NodeType.UNDEFINED -> {
                // no-op
            }
            NodeType.OBJECT_ID -> {
                val value = domModule.getObjectIdNodeValue(node)
                out.write(value)
            }
            NodeType.FALSE -> out.writeByte(NULL_BYTE)
            NodeType.TRUE -> out.writeByte(0x01)
            NodeType.UTC_DATE_TIME -> {
                val value = domModule.getUtcDateTimeNodeValue(node)
                out.writeLittleEndianLong(value)
            }
            NodeType.NULL -> {
                // no-op
            }
            NodeType.REGULAR_EXPRESSION -> {
                val regex = domModule.getRegularExpressionNodeExpression(node)
                val options = domModule.getRegularExpressionNodeOptions(node)
                out.writeCString(regex)
                out.writeCString(options)
            }
            NodeType.DB_POINTER -> {
                val name = domModule.getDbPointerNodeName(node)
                val value = domModule.getDbPointerNodeValue(node)
                out.writeString(name)
                out.write(value)
            }
            NodeType.JAVA_SCRIPT -> {
                val content = domModule.getJavaScriptNodeScriptContent(node)
                out.writeString(content)
            }
            NodeType.SYMBOL -> {
                val symbol = domModule.getSymbolNodeValue(node)
                out.writeString(symbol)
            }
            NodeType.JAVA_SCRIPT_WITH_SCOPE -> {
                writeJavaScriptWithScopeValue(node, out, sizeMarkers, domModule)
            }
            NodeType.INT32 -> {
                val value = domModule.getInt32NodeValue(node)
                out.writeLittleEndianInt(value)
            }
            NodeType.TIMESTAMP -> {
                val value = domModule.getTimestampNodeValue(node)
                out.writeLittleEndianLong(value)
            }
            NodeType.INT64 -> {
                val value = domModule.getInt64NodeValue(node)
                out.writeLittleEndianLong(value)
            }
            NodeType.DECIMAL_128 -> {
                val value = domModule.getDecimal128NodeValue(node)
                out.write(value)
            }
            NodeType.MAX_KEY -> {
                // no-op
            }
            NodeType.MIN_KEY -> {
                // no-op
            }
        }
    }

    private fun OutputStream.writeString(string: String): OutputStream {
        val bytes = string.toByteArray()
        this.writeLittleEndianInt(bytes.size + 1) // +1 for NULL terminator
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