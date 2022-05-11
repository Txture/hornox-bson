package io.txture.hornoxbson

import io.txture.hornoxbson.ByteExtensions.NULL_BYTE
import io.txture.hornoxbson.model.*
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
        val buffer = createBsonByteBuffer(byteArray)
        if (path.isEmpty()) {
            // the empty path addresses the whole document.
            return deserializeBsonDocument(buffer, false)
        }
        // skip over the total document length, it's of no interest here.
        buffer.skip(Int.SIZE_BYTES)
        return extractBsonNodeFromFieldList(buffer, path, 0, trustSizeMarkers)
    }

    private fun extractBsonNodeFromFieldList(buffer: ByteBuffer, path: List<String>, pathIndex: Int, trustSizeMarkers: Boolean): BsonNode? {
        val pathElement = path[pathIndex]
        while (true) {
            val fingerprintByte = buffer.getByte()
            if (fingerprintByte == NULL_BYTE) {
                // we've reached the end of this object/array and found no matching entry -> not found.
                return null
            }
            val fieldName = buffer.getCString()
            if (fieldName == pathElement) {
                // we do have a match. Is it the last path element?
                return if (pathIndex + 1 == path.size) {
                    // we've found the end of the path. Deserialize this node only.
                    parseFieldValue(fingerprintByte, buffer)
                } else {
                    // we have a match, but it's not the last path element
                    if (fingerprintByte == DocumentNode.FINGERPRINT_BYTE || fingerprintByte == ArrayNode.FINGERPRINT_BYTE) {
                        // need to step into this object/array.

                        // Both arrays and objects are the same in binary representation, and they
                        // always start with the object length in bytes (which we don't care about).
                        // Discard that value from the buffer.
                        buffer.skip(Int.SIZE_BYTES)

                        // continue stepping with the next path index.
                        if (fingerprintByte == ArrayNode.FINGERPRINT_BYTE && path[pathIndex + 1].toIntOrNull() == null) {
                            // the caller used a text that doesn't contain a number to search in an array node -> fast exit, that can't work.
                            return null
                        }
                        extractBsonNodeFromFieldList(buffer, path, pathIndex + 1, trustSizeMarkers)
                    } else {
                        // we're not at the end of the desired path, but we've got no further nodes to navigate into.
                        // in other words: the number of steps in the given path is larger than the nesting depth at this point in the document.
                        null
                    }
                }
            }
            // we're not at the desired field. Navigate to the next one. How far we have to go depends on the type of node we're at...
            skipSingleFieldValue(fingerprintByte, buffer, trustSizeMarkers)
        }
    }

    private fun skipSingleFieldValue(fingerprintByte: Byte, buffer: ByteBuffer, trustSizeMarkers: Boolean) {
        when (fingerprintByte) {
            DoubleNode.FINGERPRINT_BYTE -> {
                buffer.skip(Double.SIZE_BYTES)
            }
            TextNode.FINGERPRINT_BYTE -> {
                // string lengths are always accurate, even if "trustSizeMarkers" is FALSE.
                val stringLength = buffer.getInt()
                buffer.skip(stringLength)
            }
            DocumentNode.FINGERPRINT_BYTE, ArrayNode.FINGERPRINT_BYTE -> {
                // Documents (objects) and Arrays encode their total length as a leading integer.
                // However, we might not be able to trust those values because they may not be up-to-date.
                val assignedLength = buffer.getInt()
                if (assignedLength > 0 && trustSizeMarkers) {
                    // we should trust the value. Skip over the entire document without scanning it.
                    buffer.skip(assignedLength - Int.SIZE_BYTES) // the size includes the integer that states the size, we already read that.
                } else {
                    // we have no size information given, scan through the document and skip it
                    skipElementListOfUnknownSizeAndNullTerminator(buffer)
                }
            }
            BinaryNode.FINGERPRINT_BYTE -> {
                // byte array lengths are always accurate, even if "trustSizeMarkers" is FALSE.
                val byteArrayLength = buffer.getInt()
                buffer.skip(1) // skip over the subtype indicator
                buffer.skip(byteArrayLength)
            }
            UndefinedNode.FINGERPRINT_BYTE -> {
                // we're already at the correct position, the UNDEFINED node has no details.
            }
            ObjectIdNode.FINGERPRINT_BYTE -> {
                buffer.skip(ObjectIdNode.SIZE_BYTES)
            }
            BooleanNode.FINGERPRINT_BYTE -> {
                buffer.skip(1)
            }
            UtcDateTimeNode.FINGERPRINT_BYTE -> {
                buffer.skip(Long.SIZE_BYTES)
            }
            NullNode.FINGERPRINT_BYTE -> {
                // we're already at the correct position, the NULL node has no details.
            }
            RegularExpressionNode.FINGERPRINT_BYTE -> {
                // two CStrings here, one for the regex, one for the options
                // skip over the regex
                buffer.skipCString()
                // skip over the options
                buffer.skipCString()
            }
            DbPointerNode.FINGERPRINT_BYTE -> {
                val stringLength = buffer.getInt()
                buffer.skip(stringLength)
                buffer.skip(DbPointerNode.BINARY_PART_SIZE_BYTES)
            }
            JavaScriptNode.FINGERPRINT_BYTE -> {
                val stringLength = buffer.getInt()
                buffer.skip(stringLength)
            }
            SymbolNode.FINGERPRINT_BYTE -> {
                val stringLength = buffer.getInt()
                buffer.skip(stringLength)
            }
            JavaScriptWithScopeNode.FINGERPRINT_BYTE -> {
                val assignedLength = buffer.getInt()
                if (assignedLength > 0 && trustSizeMarkers) {
                    // we should trust the value. Skip over the entire entry without scanning it.
                    buffer.skip(assignedLength - Int.SIZE_BYTES) // the size includes the integer that states the size, we already read that.
                } else {
                    // we have no size information given, scan through the document and skip it
                    // skip the code string
                    buffer.skip(Int.SIZE_BYTES) // skip the code string length
                    buffer.skipCString()        // skip the code string itself
                    buffer.skip(Int.SIZE_BYTES) // skip over the length field of the scope document
                    skipElementListOfUnknownSizeAndNullTerminator(buffer) // skip over the scope document itself
                }
            }
            Int32Node.FINGERPRINT_BYTE -> {
                buffer.skip(Int.SIZE_BYTES)
            }
            TimestampNode.FINGERPRINT_BYTE -> {
                buffer.skip(Long.SIZE_BYTES)
            }
            Int64Node.FINGERPRINT_BYTE -> {
                buffer.skip(Long.SIZE_BYTES)
            }
            Decimal128Node.FINGERPRINT_BYTE -> {
                buffer.skip(Decimal128Node.SIZE_BYTES)
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

    private fun skipElementListOfUnknownSizeAndNullTerminator(buffer: ByteBuffer) {
        while (true) {
            val fingerprintByte = buffer.getByte()
            if (fingerprintByte == NULL_BYTE) {
                // we've reached the end of the document node
                return
            }
            // skip the field name, we don't care
            buffer.skipCString()
            skipSingleFieldValue(fingerprintByte, buffer, trustSizeMarkers = false)
        }
    }

    /**
     * Deserializes the [DocumentNode] contained in the given [byteBuffer] in BSON format.
     *
     * As the BSON Specification demands that all BSON byte arrays have to
     * be encoded in Little Endian order, the byte buffer will be switched
     * to [ByteOrder.LITTLE_ENDIAN] automatically. The current position of
     * the [byteBuffer] will be respected and parsing will terminate when
     * the document is complete, even if there are more bytes to read in
     * the buffer. Upon successful parsing, the position of the buffer will
     * be after the final `0x00` terminator byte of the document.
     *
     * @param byteBuffer The buffer to read from. Will be switched to [ByteOrder.LITTLE_ENDIAN].
     *
     * @return The parsed document node
     */
    @JvmStatic
    fun deserializeBsonDocument(byteBuffer: ByteBuffer): DocumentNode {
        val buffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        return deserializeBsonDocument(buffer, false) as DocumentNode
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
        val buffer = createBsonByteBuffer(byteArray)
        return deserializeBsonDocument(buffer, false) as DocumentNode
    }

    private fun createBsonByteBuffer(byteArray: ByteArray): ByteBuffer {
        // BSON is always little-endian, by definition
        return ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
    }

    private fun deserializeBsonDocument(buffer: ByteBuffer, isArray: Boolean): BsonNode {
        val length = buffer.getInt()
        val currentNode = if (isArray) {
            ArrayNode(length, mutableListOf())
        } else {
            DocumentNode(length, mutableMapOf())
        }
        var fingerprintByte = buffer.getByte()
        while (fingerprintByte != NULL_BYTE) {
            val fieldName = buffer.getCString()
            if (isArray) {
                val index = fieldName.toIntOrNull()
                requireNotNull(index) {
                    "Encountered non-integer field name for array!"
                }
            }
            val valueNode = parseFieldValue(fingerprintByte, buffer)
            if (isArray) {
                (currentNode as ArrayNode).fields.add(valueNode)
            } else {
                (currentNode as DocumentNode).fields[fieldName] = valueNode
            }
            // go to the next entry
            fingerprintByte = buffer.getByte()
        }
        return currentNode
    }

    private fun parseFieldValue(fingerprintByte: Byte, buffer: ByteBuffer): BsonNode {
        return when (fingerprintByte) {
            DoubleNode.FINGERPRINT_BYTE -> {
                DoubleNode(buffer.getDouble())
            }
            TextNode.FINGERPRINT_BYTE -> {
                TextNode(buffer.getString())
            }
            DocumentNode.FINGERPRINT_BYTE -> {
                deserializeBsonDocument(buffer, isArray = false)
            }
            ArrayNode.FINGERPRINT_BYTE -> {
                deserializeBsonDocument(buffer, isArray = true)
            }
            BinaryNode.FINGERPRINT_BYTE -> {
                val binaryLength = buffer.getInt()
                val subType = BinarySubtype.fromByte(buffer.getByte())
                val start = buffer.position()
                val end = start + binaryLength
                val binaryData = buffer.array().sliceArray(start until end)
                buffer.position(end)
                BinaryNode(binaryData, subType)
            }
            UndefinedNode.FINGERPRINT_BYTE -> {
                UndefinedNode
            }
            ObjectIdNode.FINGERPRINT_BYTE -> {
                val bytes = buffer.getByteArrayOfLength(ObjectIdNode.SIZE_BYTES)
                ObjectIdNode(bytes)
            }
            BooleanNode.FINGERPRINT_BYTE -> {
                when (buffer.getByte()) {
                    NULL_BYTE -> FalseNode
                    0x01.toByte() -> TrueNode
                    else -> throw IllegalArgumentException("Failed to parse boolean!")
                }
            }
            UtcDateTimeNode.FINGERPRINT_BYTE -> {
                UtcDateTimeNode(buffer.getLong())
            }
            NullNode.FINGERPRINT_BYTE -> {
                NullNode
            }
            RegularExpressionNode.FINGERPRINT_BYTE -> {
                val regexContent = buffer.getCString()
                val options = buffer.getCString()
                RegularExpressionNode(regexContent, options)
            }
            DbPointerNode.FINGERPRINT_BYTE -> {
                val name = buffer.getString()
                val bytes = buffer.getByteArrayOfLength(DbPointerNode.BINARY_PART_SIZE_BYTES)
                return DbPointerNode(name, bytes)
            }
            JavaScriptNode.FINGERPRINT_BYTE -> {
                return JavaScriptNode(buffer.getString())
            }
            SymbolNode.FINGERPRINT_BYTE -> {
                return SymbolNode(buffer.getString())
            }
            JavaScriptWithScopeNode.FINGERPRINT_BYTE -> {
                deserializeJavaScriptWithScope(buffer)
            }
            Int32Node.FINGERPRINT_BYTE -> {
                Int32Node(buffer.getInt())
            }
            TimestampNode.FINGERPRINT_BYTE -> {
                TimestampNode(buffer.getLong())
            }
            Int64Node.FINGERPRINT_BYTE -> {
                Int64Node(buffer.getLong())
            }
            Decimal128Node.FINGERPRINT_BYTE -> {
                Decimal128Node(buffer.getByteArrayOfLength(Decimal128Node.SIZE_BYTES))
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

    private fun deserializeJavaScriptWithScope(buffer: ByteBuffer): JavaScriptWithScopeNode {
        buffer.getInt() // skip over the total length of the node.
        val javaScript = buffer.getString()
        // deserialize the scope document
        val scope = deserializeBsonDocument(buffer, isArray = false)
        return JavaScriptWithScopeNode(javaScript, scope as DocumentNode)
    }

    private fun ByteBuffer.getString(): String {
        val stringLength = this.getInt()
        val result = String(this.array(), this.position(), stringLength - 1) // -1 to ignore terminating 0x00
        this.position(this.position() + stringLength)
        return result
    }

    private fun ByteBuffer.skipCString(): ByteBuffer {
        while (get() != NULL_BYTE) {
            // no-op
        }
        return this
    }

    private fun ByteBuffer.getCString(): String {
        val start = position()
        var stringLength = 0
        while (get() != NULL_BYTE) {
            stringLength++
        }
        return String(array(), start, stringLength)
    }

    private fun ByteBuffer.getByte(): Byte {
        return this.get()
    }

    private fun ByteBuffer.skip(numberOfBytes: Int): ByteBuffer {
        return this.position(this.position() + numberOfBytes)
    }

    private fun ByteBuffer.getByteArrayOfLength(length: Int): ByteArray {
        val array = ByteArray(length)
        repeat(length) { i ->
            array[i] = this.getByte()
        }
        return array
    }

}