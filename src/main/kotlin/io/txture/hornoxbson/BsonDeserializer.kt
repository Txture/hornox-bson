package io.txture.hornoxbson

import io.txture.hornoxbson.ByteExtensions.NULL_BYTE
import io.txture.hornoxbson.model.*
import io.txture.hornoxbson.util.ByteBufferInputStream
import io.txture.hornoxbson.util.InputStreamExtensions.readByte
import io.txture.hornoxbson.util.LittleEndianExtensions.readLittleEndianDouble
import io.txture.hornoxbson.util.LittleEndianExtensions.readLittleEndianInt
import io.txture.hornoxbson.util.LittleEndianExtensions.readLittleEndianLong
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Deserializer / Parser for BSON.
 *
 * Converts binary representations into BSON [DocumentNode]s and may also be used to extract individual
 * nodes via paths.
 */
object BsonDeserializer {

    /**
     * Extracts a single [BsonNode] from the given (BSON-formatted) [byteArray] by following the specified [path].
     *
     * @param byteArray The BSON-formatted byte array to scan.
     * @param path The path to follow to find the desired target node.
     * Each path element is either a field name (to step through [DocumentNode]s)
     * or an integer in string representation (to step through [ArrayNode]s).
     * If the path list is empty, the whole document will be parsed and returned.
     * @param trustSizeMarkers If you know for sure that the BSON [byteArray] contains valid size markers,
     * then setting [trustSizeMarkers] to `true` will enhance the scan performance. However,
     * if your BSON data does not contain valid size markers and this parameter is set to `true`,
     * arbitrary failures or invalid results may occur. Defaults to `false`.
     *
     * @return The [BsonNode] which was extracted from the given [byteArray] at the given [path],
     * or `null` if the path did not designate a valid location within the document.
     * Please note that the result can also be a [NullNode] (if the path points to it).
     */
    @JvmStatic
    @JvmOverloads
    fun extractBsonNode(byteArray: ByteArray, path: List<String>, trustSizeMarkers: Boolean = false): BsonNode? {
        require(path.none { it.isEmpty() }) { "No entry of the 'path' may be empty!" }
        ByteArrayInputStream(byteArray).use { inputStream ->
            if (path.isEmpty()) {
                // the empty path addresses the whole document.
                return deserializeBsonDocument(inputStream, false)
            }
            // skip over the total document length, it's of no interest here.
            inputStream.skip(Int.SIZE_BYTES)
            return extractBsonNodeFromFieldList(inputStream, path, 0, trustSizeMarkers)
        }
    }

    /**
     * Extracts a single [BsonNode] from the given (BSON-formatted) [inputStream] by following the specified [path].
     *
     * @param inputStream The BSON-formatted byte array to scan.
     * @param path The path to follow to find the desired target node.
     * Each path element is either a field name (to step through [DocumentNode]s)
     * or an integer in string representation (to step through [ArrayNode]s).
     * If the path list is empty, the whole document will be parsed and returned.
     * @param trustSizeMarkers If you know for sure that the BSON [inputStream] contains valid size markers,
     * then setting [trustSizeMarkers] to `true` will enhance the scan performance. However,
     * if your BSON data does not contain valid size markers and this parameter is set to `true`,
     * arbitrary failures or invalid results may occur. Defaults to `false`.
     *
     * @return The [BsonNode] which was extracted from the given [inputStream] at the given [path],
     * or `null` if the path did not designate a valid location within the document.
     * Please note that the result can also be a [NullNode] (if the path points to it).
     */
    @JvmStatic
    @JvmOverloads
    fun extractBsonNode(inputStream: InputStream, path: List<String>, trustSizeMarkers: Boolean = false): BsonNode? {
        require(path.none { it.isEmpty() }) { "No entry of the 'path' may be empty!" }
        if (path.isEmpty()) {
            // the empty path addresses the whole document.
            return deserializeBsonDocument(inputStream, isArray = false)
        }
        // skip over the total document length, it's of no interest here.
        inputStream.skip(Int.SIZE_BYTES)
        return extractBsonNodeFromFieldList(inputStream, path, 0, trustSizeMarkers)
    }

    private fun extractBsonNodeFromFieldList(inputStream: InputStream, path: List<String>, pathIndex: Int, trustSizeMarkers: Boolean): BsonNode? {
        val pathElement = path[pathIndex]
        while (true) {
            val fingerprintByte = inputStream.readByte()
            if (fingerprintByte == NULL_BYTE) {
                // we've reached the end of this object/array and found no matching entry -> not found.
                return null
            }
            val fieldName = inputStream.readCString()
            if (fieldName == pathElement) {
                // we do have a match. Is it the last path element?
                return if (pathIndex + 1 == path.size) {
                    // we've found the end of the path. Deserialize this node only.
                    parseFieldValue(fingerprintByte, inputStream)
                } else {
                    // we have a match, but it's not the last path element
                    if (fingerprintByte == DocumentNode.FINGERPRINT_BYTE || fingerprintByte == ArrayNode.FINGERPRINT_BYTE) {
                        // need to step into this object/array.

                        // Both arrays and objects are the same in binary representation, and they
                        // always start with the object length in bytes (which we don't care about).
                        // Discard that value from the buffer.
                        inputStream.skip(Int.SIZE_BYTES)

                        // continue stepping with the next path index.
                        if (fingerprintByte == ArrayNode.FINGERPRINT_BYTE && path[pathIndex + 1].toIntOrNull() == null) {
                            // the caller used a text that doesn't contain a number to search in an array node -> fast exit, that can't work.
                            return null
                        }
                        extractBsonNodeFromFieldList(inputStream, path, pathIndex + 1, trustSizeMarkers)
                    } else {
                        // we're not at the end of the desired path, but we've got no further nodes to navigate into.
                        // in other words: the number of steps in the given path is larger than the nesting depth at this point in the document.
                        null
                    }
                }
            }
            // we're not at the desired field. Navigate to the next one. How far we have to go depends on the type of node we're at...
            skipSingleFieldValue(fingerprintByte, inputStream, trustSizeMarkers)
        }
    }

    private fun skipSingleFieldValue(fingerprintByte: Byte, inputStream: InputStream, trustSizeMarkers: Boolean) {
        when (fingerprintByte) {
            DoubleNode.FINGERPRINT_BYTE -> {
                inputStream.skip(Double.SIZE_BYTES)
            }
            TextNode.FINGERPRINT_BYTE -> {
                // string lengths are always accurate, even if "trustSizeMarkers" is FALSE.
                val stringLength = inputStream.readLittleEndianInt()
                inputStream.skip(stringLength)
            }
            DocumentNode.FINGERPRINT_BYTE, ArrayNode.FINGERPRINT_BYTE -> {
                // Documents (objects) and Arrays encode their total length as a leading integer.
                // However, we might not be able to trust those values because they may not be up-to-date.
                val assignedLength = inputStream.readLittleEndianInt()
                if (assignedLength > 0 && trustSizeMarkers) {
                    // we should trust the value. Skip over the entire document without scanning it.
                    inputStream.skip(assignedLength - Int.SIZE_BYTES) // the size includes the integer that states the size, we already read that.
                } else {
                    // we have no size information given, scan through the document and skip it
                    skipElementListOfUnknownSizeAndNullTerminator(inputStream)
                }
            }
            BinaryNode.FINGERPRINT_BYTE -> {
                // byte array lengths are always accurate, even if "trustSizeMarkers" is FALSE.
                val byteArrayLength = inputStream.readLittleEndianInt()
                inputStream.skip(1) // skip over the subtype indicator
                inputStream.skip(byteArrayLength)
            }
            UndefinedNode.FINGERPRINT_BYTE -> {
                // we're already at the correct position, the UNDEFINED node has no details.
            }
            ObjectIdNode.FINGERPRINT_BYTE -> {
                inputStream.skip(ObjectIdNode.SIZE_BYTES)
            }
            BooleanNode.FINGERPRINT_BYTE -> {
                inputStream.skip(1)
            }
            UtcDateTimeNode.FINGERPRINT_BYTE -> {
                inputStream.skip(Long.SIZE_BYTES)
            }
            NullNode.FINGERPRINT_BYTE -> {
                // we're already at the correct position, the NULL node has no details.
            }
            RegularExpressionNode.FINGERPRINT_BYTE -> {
                // two CStrings here, one for the regex, one for the options
                // skip over the regex
                inputStream.skipCString()
                // skip over the options
                inputStream.skipCString()
            }
            DbPointerNode.FINGERPRINT_BYTE -> {
                val arrayLength = inputStream.readLittleEndianInt()
                inputStream.skip(arrayLength)
                inputStream.skip(DbPointerNode.BINARY_PART_SIZE_BYTES)
            }
            JavaScriptNode.FINGERPRINT_BYTE -> {
                val stringLength = inputStream.readLittleEndianInt()
                inputStream.skip(stringLength)
            }
            SymbolNode.FINGERPRINT_BYTE -> {
                val stringLength = inputStream.readLittleEndianInt()
                inputStream.skip(stringLength)
            }
            JavaScriptWithScopeNode.FINGERPRINT_BYTE -> {
                val assignedLength = inputStream.readLittleEndianInt()
                if (assignedLength > 0 && trustSizeMarkers) {
                    // we should trust the value. Skip over the entire entry without scanning it.
                    inputStream.skip(assignedLength - Int.SIZE_BYTES) // the size includes the integer that states the size, we already read that.
                } else {
                    // we have no size information given, scan through the document and skip it
                    // skip the code string
                    inputStream.skip(Int.SIZE_BYTES) // skip the code string length
                    inputStream.skipCString()        // skip the code string itself
                    inputStream.skip(Int.SIZE_BYTES) // skip over the length field of the scope document
                    skipElementListOfUnknownSizeAndNullTerminator(inputStream) // skip over the scope document itself
                }
            }
            Int32Node.FINGERPRINT_BYTE -> {
                inputStream.skip(Int.SIZE_BYTES)
            }
            TimestampNode.FINGERPRINT_BYTE -> {
                inputStream.skip(Long.SIZE_BYTES)
            }
            Int64Node.FINGERPRINT_BYTE -> {
                inputStream.skip(Long.SIZE_BYTES)
            }
            Decimal128Node.FINGERPRINT_BYTE -> {
                inputStream.skip(Decimal128Node.SIZE_BYTES)
            }
            MinKeyNode.FINGERPRINT_BYTE -> {
                // we're already at the correct position, the MinKey node has no details.
            }
            MaxKeyNode.FINGERPRINT_BYTE -> {
                // we're already at the correct position, the MaxKey node has no details.
            }
            else -> throw IllegalArgumentException("Unknown field type: ${fingerprintByte}")
        }
    }

    private fun skipElementListOfUnknownSizeAndNullTerminator(inputStream: InputStream) {
        while (true) {
            val fingerprintByte = inputStream.readByte()
            if (fingerprintByte == NULL_BYTE) {
                // we've reached the end of the document node
                return
            }
            // skip the field name, we don't care
            inputStream.skipCString()
            skipSingleFieldValue(fingerprintByte, inputStream, trustSizeMarkers = false)
        }
    }

    /**
     * Deserializes the [DocumentNode] contained in the given [byteBuffer] in BSON format.
     *
     * The current position of the [byteBuffer] will be respected and parsing will terminate when
     * the document is complete, even if there are more bytes to read in the buffer.
     * Upon successful parsing, the position of the buffer will be after the final `0x00`
     * terminator byte of the document.
     *
     * @param byteBuffer The buffer to read from.
     *
     * @return The parsed document node
     */
    @JvmStatic
    fun deserializeBsonDocument(byteBuffer: ByteBuffer): DocumentNode {
        val inputStream = ByteBufferInputStream(byteBuffer)
        return deserializeBsonDocument(inputStream, false) as DocumentNode
    }

    /**
     * Deserializes the given [byteArray] in BSON format.
     *
     * Parsing will start at index 0, and will terminate as soon
     * as the final `0x00` terminator of the document is reached.
     * Any further content that may exist in the array will be
     * ignored.
     *
     * @param byteArray The array to parse
     *
     * @return The parsed document.
     */
    @JvmStatic
    fun deserializeBsonDocument(byteArray: ByteArray): DocumentNode {
        ByteArrayInputStream(byteArray).use { bais ->
            return deserializeBsonDocument(bais)
        }
    }

    /**
     * Deserializes the data in the given [inputStream] in BSON format.
     *
     * Parsing will terminate as soon as the final `0x00` terminator
     * of the document is reached. Any further content that may exist
     * in the array will be ignored.
     *
     * @param inputStream The array to parse
     *
     * @return The parsed document.
     */
    @JvmStatic
    fun deserializeBsonDocument(inputStream: InputStream): DocumentNode {
        return deserializeBsonDocument(inputStream, isArray = false) as DocumentNode
    }

    private fun createBsonByteBuffer(byteArray: ByteArray): ByteBuffer {
        // BSON is always little-endian, by definition
        return ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
    }

    private fun deserializeBsonDocument(inputStream: InputStream, isArray: Boolean): BsonNode {
        val length = inputStream.readLittleEndianInt()
        val currentNode = if (isArray) {
            ArrayNode(length, mutableListOf())
        } else {
            DocumentNode(length, mutableMapOf())
        }
        var fingerprintByte = inputStream.readByte()
        while (fingerprintByte != NULL_BYTE) {
            val fieldName = inputStream.readCString()
            if (isArray) {
                val index = fieldName.toIntOrNull()
                requireNotNull(index) {
                    "Encountered non-integer field name for array!"
                }
            }
            val valueNode = parseFieldValue(fingerprintByte, inputStream)
            if (isArray) {
                (currentNode as ArrayNode).fields.add(valueNode)
            } else {
                (currentNode as DocumentNode).fields[fieldName] = valueNode
            }
            // go to the next entry
            fingerprintByte = inputStream.readByte()
        }
        return currentNode
    }

    private fun parseFieldValue(fingerprintByte: Byte, inputStream: InputStream): BsonNode {
        return when (fingerprintByte) {
            DoubleNode.FINGERPRINT_BYTE -> {
                DoubleNode(inputStream.readLittleEndianDouble())
            }
            TextNode.FINGERPRINT_BYTE -> {
                TextNode(inputStream.readString())
            }
            DocumentNode.FINGERPRINT_BYTE -> {
                deserializeBsonDocument(inputStream, isArray = false)
            }
            ArrayNode.FINGERPRINT_BYTE -> {
                deserializeBsonDocument(inputStream, isArray = true)
            }
            BinaryNode.FINGERPRINT_BYTE -> {
                val binaryLength = inputStream.readLittleEndianInt()
                val subType = BinarySubtype.fromByte(inputStream.readByte())
                val binaryData = inputStream.getByteArrayOfLength(binaryLength)
                BinaryNode(binaryData, subType)
            }
            UndefinedNode.FINGERPRINT_BYTE -> {
                UndefinedNode
            }
            ObjectIdNode.FINGERPRINT_BYTE -> {
                val bytes = inputStream.getByteArrayOfLength(ObjectIdNode.SIZE_BYTES)
                ObjectIdNode(bytes)
            }
            BooleanNode.FINGERPRINT_BYTE -> {
                when (inputStream.readByte()) {
                    NULL_BYTE -> FalseNode
                    0x01.toByte() -> TrueNode
                    else -> throw IllegalArgumentException("Failed to parse boolean!")
                }
            }
            UtcDateTimeNode.FINGERPRINT_BYTE -> {
                UtcDateTimeNode(inputStream.readLittleEndianLong())
            }
            NullNode.FINGERPRINT_BYTE -> {
                NullNode
            }
            RegularExpressionNode.FINGERPRINT_BYTE -> {
                val regexContent = inputStream.readCString()
                val options = inputStream.readCString()
                RegularExpressionNode(regexContent, options)
            }
            DbPointerNode.FINGERPRINT_BYTE -> {
                val name = inputStream.readString()
                val bytes = inputStream.getByteArrayOfLength(DbPointerNode.BINARY_PART_SIZE_BYTES)
                return DbPointerNode(name, bytes)
            }
            JavaScriptNode.FINGERPRINT_BYTE -> {
                return JavaScriptNode(inputStream.readString())
            }
            SymbolNode.FINGERPRINT_BYTE -> {
                return SymbolNode(inputStream.readString())
            }
            JavaScriptWithScopeNode.FINGERPRINT_BYTE -> {
                deserializeJavaScriptWithScope(inputStream)
            }
            Int32Node.FINGERPRINT_BYTE -> {
                Int32Node(inputStream.readLittleEndianInt())
            }
            TimestampNode.FINGERPRINT_BYTE -> {
                TimestampNode(inputStream.readLittleEndianLong())
            }
            Int64Node.FINGERPRINT_BYTE -> {
                Int64Node(inputStream.readLittleEndianLong())
            }
            Decimal128Node.FINGERPRINT_BYTE -> {
                Decimal128Node(inputStream.getByteArrayOfLength(Decimal128Node.SIZE_BYTES))
            }
            MinKeyNode.FINGERPRINT_BYTE -> {
                MinKeyNode
            }
            MaxKeyNode.FINGERPRINT_BYTE -> {
                MaxKeyNode
            }
            else -> throw IllegalArgumentException("Unknown field type: ${fingerprintByte}")
        }
    }

    private fun deserializeJavaScriptWithScope(inputStream: InputStream): JavaScriptWithScopeNode {
        inputStream.skip(Int.SIZE_BYTES) // skip over the total length of the node.
        val javaScript = inputStream.readString()
        // deserialize the scope document
        val scope = deserializeBsonDocument(inputStream, isArray = false)
        return JavaScriptWithScopeNode(javaScript, scope as DocumentNode)
    }

    private fun InputStream.readString(): String {
        this.skip(Int.SIZE_BYTES)
        return this.readCString()
    }

    private fun InputStream.skipCString(): InputStream {
        while (readByte() != NULL_BYTE) {
            // no-op
        }
        return this
    }

    private fun InputStream.readCString(): String {
        val buffer = mutableListOf<Byte>()
        var byte = this.readByte()
        while(byte != NULL_BYTE){
            buffer += byte
            byte = this.readByte()
        }
        return String(buffer.toByteArray())
    }

    private fun ByteBuffer.getByte(): Byte {
        return this.get()
    }

    private fun InputStream.skip(numberOfBytes: Int): InputStream {
        repeat(numberOfBytes){
            this.readByte()
        }
        return this
    }

    private fun InputStream.getByteArrayOfLength(length: Int): ByteArray {
        return this.readNBytes(length)
    }

}