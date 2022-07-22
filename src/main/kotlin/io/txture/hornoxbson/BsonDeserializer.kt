package io.txture.hornoxbson

import io.txture.hornoxbson.ByteExtensions.NULL_BYTE
import io.txture.hornoxbson.dommodule.BsonDomModule
import io.txture.hornoxbson.dommodule.HornoxDomModule
import io.txture.hornoxbson.model.*
import io.txture.hornoxbson.util.HornoxInput
import java.io.InputStream
import java.nio.ByteBuffer

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
    fun extractBsonNode(
        byteArray: ByteArray,
        path: List<String>,
        trustSizeMarkers: Boolean = false,
        startIndex: Int = 0,
        endIndex: Int = byteArray.size,
    ): BsonNode? {
        return this.extractBsonNode(
            byteArray = byteArray,
            path = path,
            trustSizeMarkers = trustSizeMarkers,
            startIndex = startIndex,
            endIndex = endIndex,
            domModule = HornoxDomModule
        )
    }

    /**
     * Extracts a single node from the given (BSON-formatted) [byteArray] by following the specified [path].
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
     * @param domModule The module to use to access or create BSON DOM elements.
     *
     * @return The [BsonNode] which was extracted from the given [byteArray] at the given [path],
     * or `null` if the path did not designate a valid location within the document.
     * Please note that the result can also be a [NullNode] (if the path points to it).
     */
    @JvmStatic
    @JvmOverloads
    fun <T> extractBsonNode(
        byteArray: ByteArray,
        path: List<String>,
        trustSizeMarkers: Boolean = false,
        startIndex: Int = 0,
        endIndex: Int = byteArray.size,
        domModule: BsonDomModule<T>,
    ): T? {
        val input = HornoxInput.fromByteArray(byteArray, trustSizeMarkers, startIndex, endIndex)
        return extractBsonNode(input, path, trustSizeMarkers, domModule)
    }

    /**
     * Extracts a single [BsonNode] from the given (BSON-formatted) [byteBuffer] by following the specified [path].
     *
     * @param byteBuffer The BSON-formatted input stream scan.
     * @param path The path to follow to find the desired target node.
     * Each path element is either a field name (to step through [DocumentNode]s)
     * or an integer in string representation (to step through [ArrayNode]s).
     * If the path list is empty, the whole document will be parsed and returned.
     * @param trustSizeMarkers If you know for sure that the BSON [byteBuffer] contains valid size markers,
     * then setting [trustSizeMarkers] to `true` will enhance the scan performance. However,
     * if your BSON data does not contain valid size markers and this parameter is set to `true`,
     * arbitrary failures or invalid results may occur. Defaults to `false`.
     *
     * @return The [BsonNode] which was extracted from the given [byteBuffer] at the given [path],
     * or `null` if the path did not designate a valid location within the document.
     * Please note that the result can also be a [NullNode] (if the path points to it).
     */
    @JvmStatic
    @JvmOverloads
    fun extractBsonNode(
        byteBuffer: ByteBuffer,
        path: List<String>,
        trustSizeMarkers: Boolean = false,
    ): BsonNode? {
        val input = HornoxInput.fromByteBuffer(byteBuffer)
        return extractBsonNode(input, path, trustSizeMarkers, HornoxDomModule)
    }

    /**
     * Extracts a single [BsonNode] from the given (BSON-formatted) [byteBuffer] by following the specified [path].
     *
     * @param byteBuffer The BSON-formatted input stream scan.
     * @param path The path to follow to find the desired target node.
     * Each path element is either a field name (to step through [DocumentNode]s)
     * or an integer in string representation (to step through [ArrayNode]s).
     * If the path list is empty, the whole document will be parsed and returned.
     * @param trustSizeMarkers If you know for sure that the BSON [byteBuffer] contains valid size markers,
     * then setting [trustSizeMarkers] to `true` will enhance the scan performance. However,
     * if your BSON data does not contain valid size markers and this parameter is set to `true`,
     * arbitrary failures or invalid results may occur. Defaults to `false`.
     * @param domModule The module to use to access or create BSON DOM elements.
     *
     * @return The [BsonNode] which was extracted from the given [byteBuffer] at the given [path],
     * or `null` if the path did not designate a valid location within the document.
     * Please note that the result can also be a [NullNode] (if the path points to it).
     */
    @JvmStatic
    @JvmOverloads
    fun <T> extractBsonNode(
        byteBuffer: ByteBuffer,
        path: List<String>,
        trustSizeMarkers: Boolean = false,
        domModule: BsonDomModule<T>,
    ): T? {
        val input = HornoxInput.fromByteBuffer(byteBuffer)
        return extractBsonNode(input, path, trustSizeMarkers, domModule)
    }

    /**
     * Extracts a single [BsonNode] from the given (BSON-formatted) [inputStream] by following the specified [path].
     *
     * @param inputStream The BSON-formatted input stream scan.
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
    fun extractBsonNode(
        inputStream: InputStream,
        path: List<String>,
        trustSizeMarkers: Boolean = false,
    ): BsonNode? {
        val input = HornoxInput.fromInputStream(inputStream)
        return extractBsonNode(input, path, trustSizeMarkers, HornoxDomModule)
    }

    /**
     * Extracts a single [BsonNode] from the given (BSON-formatted) [inputStream] by following the specified [path].
     *
     * @param inputStream The BSON-formatted input stream scan.
     * @param path The path to follow to find the desired target node.
     * Each path element is either a field name (to step through [DocumentNode]s)
     * or an integer in string representation (to step through [ArrayNode]s).
     * If the path list is empty, the whole document will be parsed and returned.
     * @param trustSizeMarkers If you know for sure that the BSON [inputStream] contains valid size markers,
     * then setting [trustSizeMarkers] to `true` will enhance the scan performance. However,
     * if your BSON data does not contain valid size markers and this parameter is set to `true`,
     * arbitrary failures or invalid results may occur. Defaults to `false`.
     * @param bsonDomModule The module to use to access or create BSON DOM elements.
     *
     * @return The [BsonNode] which was extracted from the given [inputStream] at the given [path],
     * or `null` if the path did not designate a valid location within the document.
     * Please note that the result can also be a [NullNode] (if the path points to it).
     */
    @JvmStatic
    @JvmOverloads
    fun <T> extractBsonNode(
        inputStream: InputStream,
        path: List<String>,
        trustSizeMarkers: Boolean = false,
        bsonDomModule: BsonDomModule<T>,
    ): T? {
        val input = HornoxInput.fromInputStream(inputStream)
        return extractBsonNode(input, path, trustSizeMarkers, bsonDomModule)
    }

    @JvmStatic
    private fun <T> extractBsonNode(
        input: HornoxInput,
        path: List<String>,
        trustSizeMarkers: Boolean = false,
        domModule: BsonDomModule<T>,
    ): T? {
        require(path.none { it.isEmpty() }) { "No entry of the 'path' may be empty!" }
        if (path.isEmpty()) {
            // the empty path addresses the whole document.
            return deserializeBsonDocument(input, domModule, isArray = false)
        }
        // skip over the total document length, it's of no interest here.
        input.skipBytes(Int.SIZE_BYTES)
        return extractBsonNodeFromFieldList(input, domModule, path, 0, trustSizeMarkers)
    }

    private fun <T> extractBsonNodeFromFieldList(
        input: HornoxInput,
        domModule: BsonDomModule<T>,
        path: List<String>,
        pathIndex: Int,
        trustSizeMarkers: Boolean,
    ): T? {
        val pathElement = path[pathIndex]
        while (true) {
            val fingerprintByte = input.readByte()
            if (fingerprintByte == NULL_BYTE) {
                // we've reached the end of this object/array and found no matching entry -> not found.
                return null
            }
            val fieldName = input.readCString()
            if (fieldName == pathElement) {
                // we do have a match. Is it the last path element?
                return if (pathIndex + 1 == path.size) {
                    // we've found the end of the path. Deserialize this node only.
                    parseFieldValue(fingerprintByte, input, domModule)
                } else {
                    // we have a match, but it's not the last path element
                    if (fingerprintByte == NodeType.DOCUMENT.fingerprintByte || fingerprintByte == NodeType.ARRAY.fingerprintByte) {
                        // need to step into this object/array.

                        // Both arrays and objects are the same in binary representation, and they
                        // always start with the object length in bytes (which we don't care about).
                        // Discard that value from the buffer.
                        input.skipBytes(Int.SIZE_BYTES)

                        // continue stepping with the next path index.
                        if (fingerprintByte == NodeType.ARRAY.fingerprintByte && path[pathIndex + 1].toIntOrNull() == null) {
                            // the caller used a text that doesn't contain a number to search in an array node -> fast exit, that can't work.
                            return null
                        }
                        extractBsonNodeFromFieldList(input, domModule, path, pathIndex + 1, trustSizeMarkers)
                    } else {
                        // we're not at the end of the desired path, but we've got no further nodes to navigate into.
                        // in other words: the number of steps in the given path is larger than the nesting depth at this point in the document.
                        null
                    }
                }
            }
            // we're not at the desired field. Navigate to the next one. How far we have to go depends on the type of node we're at...
            skipSingleFieldValue(fingerprintByte, input, trustSizeMarkers)
        }
    }

    private fun skipSingleFieldValue(fingerprintByte: Byte, input: HornoxInput, trustSizeMarkers: Boolean) {
        when (fingerprintByte) {
            NodeType.DOUBLE.fingerprintByte -> {
                input.skipBytes(Double.SIZE_BYTES)
            }
            NodeType.TEXT.fingerprintByte -> {
                // string lengths are always accurate, even if "trustSizeMarkers" is FALSE.
                val stringLength = input.readLittleEndianInt()
                input.skipBytes(stringLength)
            }
            NodeType.DOCUMENT.fingerprintByte, NodeType.ARRAY.fingerprintByte -> {
                // Documents (objects) and Arrays encode their total length as a leading integer.
                // However, we might not be able to trust those values because they may not be up-to-date.
                val assignedLength = input.readLittleEndianInt()
                if (assignedLength > 0 && trustSizeMarkers) {
                    // we should trust the value. Skip over the entire document without scanning it.
                    input.skipBytes(assignedLength - Int.SIZE_BYTES) // the size includes the integer that states the size, we already read that.
                } else {
                    // we have no size information given, scan through the document and skip it
                    skipElementListOfUnknownSizeAndNullTerminator(input)
                }
            }
            NodeType.BINARY.fingerprintByte -> {
                // byte array lengths are always accurate, even if "trustSizeMarkers" is FALSE.
                val byteArrayLength = input.readLittleEndianInt()
                input.skipBytes(1) // skip over the subtype indicator
                input.skipBytes(byteArrayLength)
            }
            NodeType.UNDEFINED.fingerprintByte -> {
                // we're already at the correct position, the UNDEFINED node has no details.
            }
            NodeType.OBJECT_ID.fingerprintByte -> {
                input.skipBytes(ObjectIdNode.SIZE_BYTES)
            }
            NodeType.TRUE.fingerprintByte, NodeType.FALSE.fingerprintByte -> {
                input.skipBytes(1)
            }
            NodeType.UTC_DATE_TIME.fingerprintByte -> {
                input.skipBytes(Long.SIZE_BYTES)
            }
            NodeType.NULL.fingerprintByte -> {
                // we're already at the correct position, the NULL node has no details.
            }
            NodeType.REGULAR_EXPRESSION.fingerprintByte -> {
                // two CStrings here, one for the regex, one for the options
                // skip over the regex
                input.skipCString()
                // skip over the options
                input.skipCString()
            }
            NodeType.DB_POINTER.fingerprintByte -> {
                val arrayLength = input.readLittleEndianInt()
                input.skipBytes(arrayLength)
                input.skipBytes(DbPointerNode.BINARY_PART_SIZE_BYTES)
            }
            NodeType.JAVA_SCRIPT.fingerprintByte -> {
                val stringLength = input.readLittleEndianInt()
                input.skipBytes(stringLength)
            }
            NodeType.SYMBOL.fingerprintByte -> {
                val stringLength = input.readLittleEndianInt()
                input.skipBytes(stringLength)
            }
            NodeType.JAVA_SCRIPT_WITH_SCOPE.fingerprintByte -> {
                val assignedLength = input.readLittleEndianInt()
                if (assignedLength > 0 && trustSizeMarkers) {
                    // we should trust the value. Skip over the entire entry without scanning it.
                    input.skipBytes(assignedLength - Int.SIZE_BYTES) // the size includes the integer that states the size, we already read that.
                } else {
                    // we have no size information given, scan through the document and skip it
                    // skip the code string
                    input.skipBytes(Int.SIZE_BYTES) // skip the code string length
                    input.skipCString()        // skip the code string itself
                    input.skipBytes(Int.SIZE_BYTES) // skip over the length field of the scope document
                    skipElementListOfUnknownSizeAndNullTerminator(input) // skip over the scope document itself
                }
            }
            NodeType.INT32.fingerprintByte -> {
                input.skipBytes(Int.SIZE_BYTES)
            }
            NodeType.TIMESTAMP.fingerprintByte -> {
                input.skipBytes(Long.SIZE_BYTES)
            }
            NodeType.INT64.fingerprintByte -> {
                input.skipBytes(Long.SIZE_BYTES)
            }
            NodeType.DECIMAL_128.fingerprintByte -> {
                input.skipBytes(Decimal128Node.SIZE_BYTES)
            }
            NodeType.MIN_KEY.fingerprintByte -> {
                // we're already at the correct position, the MinKey node has no details.
            }
            NodeType.MAX_KEY.fingerprintByte -> {
                // we're already at the correct position, the MaxKey node has no details.
            }
            else -> throw IllegalArgumentException("Unknown field type: ${fingerprintByte}")
        }
    }

    private fun skipElementListOfUnknownSizeAndNullTerminator(input: HornoxInput) {
        while (true) {
            val fingerprintByte = input.readByte()
            if (fingerprintByte == NULL_BYTE) {
                // we've reached the end of the document node
                return
            }
            // skip the field name, we don't care
            input.skipCString()
            skipSingleFieldValue(fingerprintByte, input, trustSizeMarkers = false)
        }
    }

    /**
     * Deserializes a top-level node (and its contents).
     *
     * Please note that this operation is **not** conforming to the BSON standard. The BSON standard only allows [DocumentNode]s at the top level.
     * To deserialize a regular BSON document, please use [deserializeBsonDocument]. This method only exists to deserialize the nodes which were
     * written with [BsonSerializer.serializeBsonNode].
     *
     * @param inputStream The input stream to read
     *
     * @return The deserialized top-level node (including its content).
     *
     * @see [BsonSerializer.serializeBsonNode]
     */
    @JvmStatic
    fun deserializeBsonNode(inputStream: InputStream): BsonNode {
        val input = HornoxInput.fromInputStream(inputStream)
        return deserializeBsonNode(input, HornoxDomModule)
    }


    /**
     * Deserializes a top-level node (and its contents).
     *
     * Please note that this operation is **not** conforming to the BSON standard. The BSON standard only allows [DocumentNode]s at the top level.
     * To deserialize a regular BSON document, please use [deserializeBsonDocument]. This method only exists to deserialize the nodes which were
     * written with [BsonSerializer.serializeBsonNode].
     *
     * @param inputStream The input stream to read
     * @param domModule The DOM module used for the deserialization.
     *
     * @return The deserialized top-level node (including its content).
     *
     * @see [BsonSerializer.serializeBsonNode]
     */
    @JvmStatic
    fun <T> deserializeBsonNode(inputStream: InputStream, domModule: BsonDomModule<T>): T {
        val input = HornoxInput.fromInputStream(inputStream)
        return deserializeBsonNode(input, domModule)
    }

    /**
     * Deserializes a top-level node (and its contents).
     *
     * Please note that this operation is **not** conforming to the BSON standard. The BSON standard only allows [DocumentNode]s at the top level.
     * To deserialize a regular BSON document, please use [deserializeBsonDocument]. This method only exists to deserialize the nodes which were
     * written with [BsonSerializer.serializeBsonNode].
     *
     * @param byteArray The byte array to read
     * @param trustStringSizeMarkers Use `true` if the size markers of strings in the BSON can be trusted,
     * or use `false` if the parser should ignore them and look for null terminators instead. If a string size
     * marker is 0 or less or is found to lead to an invalid location in the input, it will always be ignored,
     * regardless of this setting.
     * @param startIndex The start index in the array for deserialization (inclusive). Defaults to 0.
     * @param endIndex The end index in the array for deserialization (exclusive). Defaults to the length of the array.
     *
     * @return The deserialized top-level node (including its content).
     *
     * @see [BsonSerializer.serializeBsonNode]
     */
    @JvmStatic
    @JvmOverloads
    fun deserializeBsonNode(byteArray: ByteArray, trustStringSizeMarkers: Boolean = false, startIndex: Int = 0, endIndex: Int = byteArray.size): BsonNode {
        val input = HornoxInput.fromByteArray(byteArray, trustStringSizeMarkers, startIndex, endIndex)
        return deserializeBsonNode(input, HornoxDomModule)
    }

    /**
     * Deserializes a top-level node (and its contents).
     *
     * Please note that this operation is **not** conforming to the BSON standard. The BSON standard only allows [DocumentNode]s at the top level.
     * To deserialize a regular BSON document, please use [deserializeBsonDocument]. This method only exists to deserialize the nodes which were
     * written with [BsonSerializer.serializeBsonNode].
     *
     * @param byteBuffer The buffer to read from.
     * @param trustStringSizeMarkers Use `true` if the size markers of strings in the BSON can be trusted,
     * or use `false` if the parser should ignore them and look for null terminators instead. If a string size
     * marker is 0 or less or is found to lead to an invalid location in the input, it will always be ignored,
     * regardless of this setting.
     * @param domModule The DOM module used for the deserialization.
     *
     * @return The deserialized top-level node (including its content).
     *
     * @see [BsonSerializer.serializeBsonNode]
     */
    @JvmStatic
    @JvmOverloads
    fun <T> deserializeBsonNode(byteBuffer: ByteBuffer, trustStringSizeMarkers: Boolean = false, domModule: BsonDomModule<T>): T {
        val input = HornoxInput.fromByteBuffer(byteBuffer, trustStringSizeMarkers)
        return deserializeBsonNode(input, domModule)
    }

    /**
     * Deserializes a top-level node (and its contents).
     *
     * Please note that this operation is **not** conforming to the BSON standard. The BSON standard only allows [DocumentNode]s at the top level.
     * To deserialize a regular BSON document, please use [deserializeBsonDocument]. This method only exists to deserialize the nodes which were
     * written with [BsonSerializer.serializeBsonNode].
     *
     * @param byteBuffer The buffer to read from.
     * @param trustStringSizeMarkers Use `true` if the size markers of strings in the BSON can be trusted,
     * or use `false` if the parser should ignore them and look for null terminators instead. If a string size
     * marker is 0 or less or is found to lead to an invalid location in the input, it will always be ignored,
     * regardless of this setting.
     *
     * @return The deserialized top-level node (including its content).
     *
     * @see [BsonSerializer.serializeBsonNode]
     */
    @JvmStatic
    @JvmOverloads
    fun deserializeBsonNode(byteBuffer: ByteBuffer, trustStringSizeMarkers: Boolean = false): BsonNode {
        val input = HornoxInput.fromByteBuffer(byteBuffer, trustStringSizeMarkers)
        return deserializeBsonNode(input, HornoxDomModule)
    }


    /**
     * Deserializes a top-level node (and its contents).
     *
     * Please note that this operation is **not** conforming to the BSON standard. The BSON standard only allows [DocumentNode]s at the top level.
     * To deserialize a regular BSON document, please use [deserializeBsonDocument]. This method only exists to deserialize the nodes which were
     * written with [BsonSerializer.serializeBsonNode].
     *
     * @param byteArray The byte array to read
     * @param trustStringSizeMarkers Use `true` if the size markers of strings in the BSON can be trusted,
     * or use `false` if the parser should ignore them and look for null terminators instead. If a string size
     * marker is 0 or less or is found to lead to an invalid location in the input, it will always be ignored,
     * regardless of this setting.
     * @param startIndex The start index in the array for deserialization (inclusive). Defaults to 0.
     * @param endIndex The end index in the array for deserialization (exclusive). Defaults to the length of the array.
     * @param domModule The DOM module used for the deserialization.
     *
     * @return The deserialized top-level node (including its content).
     *
     * @see [BsonSerializer.serializeBsonNode]
     */
    @JvmStatic
    @JvmOverloads
    fun <T> deserializeBsonNode(byteArray: ByteArray, trustStringSizeMarkers: Boolean = false, startIndex: Int = 0, endIndex: Int = byteArray.size, domModule: BsonDomModule<T>): T {
        val input = HornoxInput.fromByteArray(byteArray, trustStringSizeMarkers, startIndex, endIndex)
        return deserializeBsonNode(input, domModule)
    }


    private fun <T> deserializeBsonNode(input: HornoxInput, domModule: BsonDomModule<T>): T {
        val fingerprintByte = input.readByte()
        return this.parseFieldValue(fingerprintByte, input, domModule)
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
     * @param trustStringSizeMarkers Use `true` if the size markers of strings in the BSON can be trusted,
     * or use `false` if the parser should ignore them and look for null terminators instead. If a string size
     * marker is 0 or less or is found to lead to an invalid location in the input, it will always be ignored,
     * regardless of this setting.
     *
     * @return The parsed document node
     */
    @JvmStatic
    @JvmOverloads
    fun deserializeBsonDocument(byteBuffer: ByteBuffer, trustStringSizeMarkers: Boolean = false): DocumentNode {
        return deserializeBsonDocument(byteBuffer, trustStringSizeMarkers, HornoxDomModule) as DocumentNode
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
     * @param trustStringSizeMarkers Use `true` if the size markers of strings in the BSON can be trusted,
     * or use `false` if the parser should ignore them and look for null terminators instead. If a string size
     * marker is 0 or less or is found to lead to an invalid location in the input, it will always be ignored,
     * regardless of this setting.
     * @param domModule The DOM module used for the deserialization.
     *
     * @return The parsed document node
     */
    @JvmStatic
    @JvmOverloads
    fun <T> deserializeBsonDocument(byteBuffer: ByteBuffer, trustStringSizeMarkers: Boolean = false, domModule: BsonDomModule<T>): T {
        val input = HornoxInput.fromByteBuffer(byteBuffer, trustStringSizeMarkers)
        return deserializeBsonDocument(input, domModule, isArray = false)
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
     * @param trustStringSizeMarkers Use `true` if the size markers of strings in the BSON can be trusted,
     * or use `false` if the parser should ignore them and look for null terminators instead. If a string size
     * marker is 0 or less or is found to lead to an invalid location in the input, it will always be ignored,
     * regardless of this setting.
     * @param startIndex The start index in the array for deserialization (inclusive). Defaults to 0.
     * @param endIndex The end index in the array for deserialization (exclusive). Defaults to the length of the array.
     *
     * @return The parsed document.
     */
    @JvmStatic
    @JvmOverloads
    fun deserializeBsonDocument(
        byteArray: ByteArray,
        trustStringSizeMarkers: Boolean = false,
        startIndex: Int = 0,
        endIndex: Int = byteArray.size,
    ): DocumentNode {
        val input = HornoxInput.fromByteArray(
            byteArray = byteArray,
            trustStringSizeMarkers = trustStringSizeMarkers,
            startIndex = startIndex,
            endIndex = endIndex
        )
        return deserializeBsonDocument(input, HornoxDomModule, isArray = false) as DocumentNode
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
     * @param trustStringSizeMarkers Use `true` if the size markers of strings in the BSON can be trusted,
     * or use `false` if the parser should ignore them and look for null terminators instead. If a string size
     * marker is 0 or less or is found to lead to an invalid location in the input, it will always be ignored,
     * regardless of this setting.
     * @param startIndex The start index in the array for deserialization (inclusive). Defaults to 0.
     * @param endIndex The end index in the array for deserialization (exclusive). Defaults to the length of the array.
     * @param domModule The DOM module used for the deserialization.
     *
     * @return The parsed document.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> deserializeBsonDocument(
        byteArray: ByteArray,
        trustStringSizeMarkers: Boolean = false,
        startIndex: Int = 0,
        endIndex: Int = byteArray.size,
        domModule: BsonDomModule<T>
    ): T {
        val input = HornoxInput.fromByteArray(
            byteArray = byteArray,
            trustStringSizeMarkers = trustStringSizeMarkers,
            startIndex = startIndex,
            endIndex = endIndex
        )
        return deserializeBsonDocument(input, domModule, isArray = false)
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
        val input = HornoxInput.fromInputStream(inputStream)
        return deserializeBsonDocument(input, HornoxDomModule, isArray = false) as DocumentNode
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
    fun <T> deserializeBsonDocument(inputStream: InputStream, domModule: BsonDomModule<T>): T {
        val input = HornoxInput.fromInputStream(inputStream)
        return deserializeBsonDocument(input, domModule, isArray = false)
    }

    private fun <T> deserializeBsonDocument(input: HornoxInput, domModule: BsonDomModule<T>, isArray: Boolean): T {
        val length = input.readLittleEndianInt()
        val currentNode = if (isArray) {
            domModule.createArrayNode().also {
                domModule.setArrayNodeSizeBytes(it, length)
            }
        } else {
            domModule.createDocumentNode().also {
                domModule.setDocumentNodeSizeBytes(it, length)
            }
        }
        var fingerprintByte = input.readByte()
        while (fingerprintByte != NULL_BYTE) {
            if (isArray) {
                input.skipCString()
                val valueNode = parseFieldValue(fingerprintByte, input, domModule)
                domModule.addArrayEntry(currentNode, valueNode)
            } else {
                val fieldName = input.readCString()
                val valueNode = parseFieldValue(fingerprintByte, input, domModule)
                domModule.setDocumentNodeFieldValue(currentNode, fieldName, valueNode)
            }
            // go to the next entry
            fingerprintByte = input.readByte()
        }
        return currentNode
    }

    private fun <T> parseFieldValue(fingerprintByte: Byte, input: HornoxInput, domModule: BsonDomModule<T>): T {
        return when (fingerprintByte) {
            NodeType.DOUBLE.fingerprintByte -> {
                domModule.createDoubleNode(input.readLittleEndianDouble())
            }
            NodeType.TEXT.fingerprintByte -> {
                domModule.createTextNode(input.readString())
            }
            NodeType.DOCUMENT.fingerprintByte -> {
                deserializeBsonDocument(input, domModule, isArray = false)
            }
            NodeType.ARRAY.fingerprintByte -> {
                deserializeBsonDocument(input, domModule, isArray = true)
            }
            NodeType.BINARY.fingerprintByte -> {
                val binaryLength = input.readLittleEndianInt()
                val subType = BinarySubtype.fromByte(input.readByte())
                val binaryData = input.readByteArrayOfLength(binaryLength)
                domModule.createBinaryNode(binaryData, subType)
            }
            NodeType.UNDEFINED.fingerprintByte -> {
                domModule.createUndefinedNode()
            }
            NodeType.OBJECT_ID.fingerprintByte -> {
                val bytes = input.readByteArrayOfLength(ObjectIdNode.SIZE_BYTES)
                domModule.createObjectIdNode(bytes)
            }
            NodeType.TRUE.fingerprintByte, NodeType.FALSE.fingerprintByte -> {
                when (input.readByte()) {
                    NULL_BYTE -> domModule.createBooleanNode(false)
                    0x01.toByte() -> domModule.createBooleanNode(true)
                    else -> throw IllegalArgumentException("Failed to parse boolean!")
                }
            }
            NodeType.UTC_DATE_TIME.fingerprintByte -> {
                domModule.createUtcDateTimeNode(input.readLittleEndianLong())
            }
            NodeType.NULL.fingerprintByte -> {
                domModule.createNullNode()
            }
            NodeType.REGULAR_EXPRESSION.fingerprintByte -> {
                val regexContent = input.readCString()
                val options = input.readCString()
                domModule.createRegularExpressionNode(regexContent, options)
            }
            NodeType.DB_POINTER.fingerprintByte -> {
                val name = input.readString()
                val bytes = input.readByteArrayOfLength(DbPointerNode.BINARY_PART_SIZE_BYTES)
                domModule.createDbPointerNode(name, bytes)
            }
            NodeType.JAVA_SCRIPT.fingerprintByte -> {
                domModule.createJavaScriptNode(input.readString())
            }
            NodeType.SYMBOL.fingerprintByte -> {
                domModule.createSymbolNode(input.readString())
            }
            NodeType.JAVA_SCRIPT_WITH_SCOPE.fingerprintByte -> {
                deserializeJavaScriptWithScope(input, domModule)
            }
            NodeType.INT32.fingerprintByte -> {
                domModule.createInt32Node(input.readLittleEndianInt())
            }
            NodeType.TIMESTAMP.fingerprintByte -> {
                domModule.createTimestampNode(input.readLittleEndianLong())
            }
            NodeType.INT64.fingerprintByte -> {
                domModule.createInt64Node(input.readLittleEndianLong())
            }
            NodeType.DECIMAL_128.fingerprintByte -> {
                domModule.createDecimal128Node(input.readByteArrayOfLength(Decimal128Node.SIZE_BYTES))
            }
            NodeType.MIN_KEY.fingerprintByte -> {
                domModule.createMinKeyNode()
            }
            NodeType.MAX_KEY.fingerprintByte -> {
                domModule.createMaxKeyNode()
            }
            else -> throw IllegalArgumentException("Unknown field type: ${fingerprintByte}")
        }
    }

    private fun <T> deserializeJavaScriptWithScope(input: HornoxInput, domModule: BsonDomModule<T>): T {
        // skip over the total length of the node, we don't need it.
        input.skipBytes(Int.SIZE_BYTES)
        val javaScript = input.readString()
        // deserialize the scope document
        val scope = deserializeBsonDocument(input, domModule, isArray = false)
        return domModule.createJavaScriptWithScopeNode(javaScript, scope)
    }

}