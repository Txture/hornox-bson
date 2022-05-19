package io.txture.hornoxbson.dommodule

import io.txture.hornoxbson.model.BinarySubtype
import io.txture.hornoxbson.model.NodeType

interface BsonDomModule<N> {

    // =================================================================================================================
    // NODE TYPE
    // =================================================================================================================

    fun getNodeType(node: N): NodeType

    // =================================================================================================================
    // DOCUMENT NODE
    // =================================================================================================================

    fun getDocumentNodeSizeBytes(documentNode: N): Int

    fun getDocumentNodeFields(documentNode: N): Iterable<String>

    fun getDocumentNodeFieldValue(documentNode: N, field: String): N?

    fun createDocumentNode(): N

    fun setDocumentNodeFieldValue(documentNode: N, name: String, value: N)

    fun setDocumentNodeSizeBytes(documentNode: N, sizeBytes: Int)

    // =================================================================================================================
    // ARRAY NODE
    // =================================================================================================================

    fun getArrayNodeSizeBytes(arrayNode: N): Int

    fun getArrayNodeLength(arrayNode: N): Int

    fun getArrayNodeEntry(arrayNode: N, index: Int): N

    fun createArrayNode(): N

    fun addArrayEntry(arrayNode: N, entry: N)

    fun setArrayNodeSizeBytes(arrayNode: N, sizeBytes: Int)

    // =================================================================================================================
    // TEXT NODE
    // =================================================================================================================

    fun getTextNodeStringContent(textNode: N): String

    fun createTextNode(content: String): N

    // =================================================================================================================
    // DOUBLE NODE
    // =================================================================================================================

    fun getDoubleNodeValue(doubleNode: N): Double

    fun createDoubleNode(value: Double): N

    // =================================================================================================================
    // BINARY DATA
    // =================================================================================================================

    fun getBinaryNodeValue(binaryNode: N): ByteArray

    fun getBinaryNodeSubType(binaryNode: N): BinarySubtype

    fun createBinaryNode(value: ByteArray, binarySubtype: BinarySubtype): N

    // =================================================================================================================
    // UNDEFINED
    // =================================================================================================================
    
    fun createUndefinedNode(): N

    // =================================================================================================================
    // OBJECT ID
    // =================================================================================================================
    
    fun getObjectIdNodeValue(objectIdNode: N): ByteArray
    
    fun createObjectIdNode(value: ByteArray): N

    // =================================================================================================================
    // BOOLEAN
    // =================================================================================================================
    
    fun createBooleanNode(value: Boolean): N

    // =================================================================================================================
    // UTC DATETIME 
    // =================================================================================================================
    
    fun getUtcDateTimeNodeValue(utcDateTimeNode: N): Long
    
    fun createUtcDateTimeNode(dateTime: Long): N

    // =================================================================================================================
    // NULL
    // =================================================================================================================
    
    fun createNullNode(): N

    // =================================================================================================================
    // REGULAR EXPRESSION
    // =================================================================================================================
    
    fun getRegularExpressionNodeExpression(regularExpressionNode: N): String
    
    fun getRegularExpressionNodeOptions(regularExpressionNode: N): String
    
    fun createRegularExpressionNode(expression: String, options: String): N

    // =================================================================================================================
    // DB POINTER
    // =================================================================================================================
    
    fun getDbPointerNodeValue(dbPointerNode: N): ByteArray

    fun getDbPointerNodeName(dbPointerNode: N): String
    
    fun createDbPointerNode(name: String, value: ByteArray): N

    // =================================================================================================================
    // JAVASCRIPT
    // =================================================================================================================

    fun getJavaScriptNodeScriptContent(javaScriptNode: N): String

    fun createJavaScriptNode(scriptContent: String): N

    // =================================================================================================================
    // JAVASCRIPT WITH SCOPE
    // =================================================================================================================

    fun getJavaScriptWithScopeScriptContent(javaScriptWithScopeNode: N): String

    fun getJavaScriptWithScopeScopeDocument(javaScriptWithScopeNode: N): N

    fun createJavaScriptWithScopeNode(scriptContent: String, scopeDocumentNode: N): N

    // =================================================================================================================
    // INT32
    // =================================================================================================================

    fun getInt32NodeValue(int32Node: N): Int

    fun createInt32Node(value: Int): N

    // =================================================================================================================
    // INT64
    // =================================================================================================================

    fun getInt64NodeValue(int64Node: N): Long

    fun createInt64Node(value: Long): N

    // =================================================================================================================
    // TIMESTAMP
    // =================================================================================================================

    fun getTimestampNodeValue(timestampNode: N): Long

    fun createTimestampNode(value: Long): N

    // =================================================================================================================
    // DECIMAL 128
    // =================================================================================================================

    fun getDecimal128NodeValue(decimal128Node: N): ByteArray

    fun createDecimal128Node(value: ByteArray): N

    // =================================================================================================================
    // MIN KEY
    // =================================================================================================================

    fun createMinKeyNode(): N

    // =================================================================================================================
    // MAX KEY
    // =================================================================================================================

    fun createMaxKeyNode(): N

    // =================================================================================================================
    // SYMBOL
    // =================================================================================================================

    fun getSymbolNodeValue(symbolNode: N): String

    fun createSymbolNode(value: String): N

}