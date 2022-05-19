package io.txture.hornoxbson.dommodule

import io.txture.hornoxbson.model.*

object HornoxDomModule : BsonDomModule<BsonNode> {

    override fun getNodeType(node: BsonNode): NodeType {
        return node.nodeType
    }

    override fun getDocumentNodeSizeBytes(documentNode: BsonNode): Int {
        return (documentNode as DocumentNode).length
    }

    override fun getDocumentNodeFields(documentNode: BsonNode): Iterable<String> {
        return (documentNode as DocumentNode).keys
    }

    override fun getDocumentNodeFieldValue(documentNode: BsonNode, field: String): BsonNode? {
        return (documentNode as DocumentNode)[field]
    }

    override fun createDocumentNode(): BsonNode {
        return DocumentNode()
    }

    override fun setDocumentNodeFieldValue(documentNode: BsonNode, name: String, value: BsonNode) {
        (documentNode as DocumentNode)[name] = value
    }

    override fun setDocumentNodeSizeBytes(documentNode: BsonNode, sizeBytes: Int) {
        (documentNode as DocumentNode).length = sizeBytes
    }

    override fun getArrayNodeSizeBytes(arrayNode: BsonNode): Int {
        return (arrayNode as ArrayNode).length
    }

    override fun getArrayNodeLength(arrayNode: BsonNode): Int {
        return (arrayNode as ArrayNode).size
    }

    override fun getArrayNodeEntry(arrayNode: BsonNode, index: Int): BsonNode {
        return (arrayNode as ArrayNode)[index]
    }

    override fun createArrayNode(): BsonNode {
        return ArrayNode()
    }

    override fun addArrayEntry(arrayNode: BsonNode, entry: BsonNode) {
        (arrayNode as ArrayNode).add(entry)
    }

    override fun setArrayNodeSizeBytes(arrayNode: BsonNode, sizeBytes: Int) {
        (arrayNode as ArrayNode).length = sizeBytes
    }


    override fun getTextNodeStringContent(textNode: BsonNode): String {
        return (textNode as TextNode).value
    }

    override fun createTextNode(content: String): BsonNode {
        return TextNode(content)
    }

    override fun getDoubleNodeValue(doubleNode: BsonNode): Double {
        return (doubleNode as DoubleNode).value
    }

    override fun createDoubleNode(value: Double): BsonNode {
        return DoubleNode(value)
    }

    override fun getBinaryNodeValue(binaryNode: BsonNode): ByteArray {
        return (binaryNode as BinaryNode).value
    }

    override fun getBinaryNodeSubType(binaryNode: BsonNode): BinarySubtype {
        return (binaryNode as BinaryNode).subtype
    }

    override fun createBinaryNode(value: ByteArray, binarySubtype: BinarySubtype): BsonNode {
        return BinaryNode(value, binarySubtype)
    }

    override fun createUndefinedNode(): BsonNode {
        return UndefinedNode
    }

    override fun getObjectIdNodeValue(objectIdNode: BsonNode): ByteArray {
        return (objectIdNode as ObjectIdNode).value
    }

    override fun createObjectIdNode(value: ByteArray): BsonNode {
        return ObjectIdNode(value)
    }

    override fun createBooleanNode(value: Boolean): BsonNode {
        return if(value){
            TrueNode
        }else{
            FalseNode
        }
    }

    override fun getUtcDateTimeNodeValue(utcDateTimeNode: BsonNode): Long {
        return (utcDateTimeNode as UtcDateTimeNode).value
    }

    override fun createUtcDateTimeNode(dateTime: Long): BsonNode {
        return UtcDateTimeNode(dateTime)
    }

    override fun createNullNode(): BsonNode {
        return NullNode
    }

    override fun getRegularExpressionNodeExpression(regularExpressionNode: BsonNode): String {
        return (regularExpressionNode as RegularExpressionNode).regularExpression
    }

    override fun getRegularExpressionNodeOptions(regularExpressionNode: BsonNode): String {
        return (regularExpressionNode as RegularExpressionNode).options
    }

    override fun createRegularExpressionNode(expression: String, options: String): BsonNode {
        return RegularExpressionNode(expression, options)
    }

    override fun getDbPointerNodeValue(dbPointerNode: BsonNode): ByteArray {
        return (dbPointerNode as DbPointerNode).value
    }

    override fun getDbPointerNodeName(dbPointerNode: BsonNode): String {
        return (dbPointerNode as DbPointerNode).name
    }

    override fun createDbPointerNode(name: String, value: ByteArray): BsonNode {
        return DbPointerNode(name, value)
    }

    override fun getJavaScriptNodeScriptContent(javaScriptNode: BsonNode): String {
        return (javaScriptNode as JavaScriptNode).value
    }

    override fun createJavaScriptNode(scriptContent: String): BsonNode {
        return JavaScriptNode(scriptContent)
    }

    override fun getJavaScriptWithScopeScriptContent(javaScriptWithScopeNode: BsonNode): String {
        return (javaScriptWithScopeNode as JavaScriptWithScopeNode).value
    }

    override fun getJavaScriptWithScopeScopeDocument(javaScriptWithScopeNode: BsonNode): BsonNode {
         return (javaScriptWithScopeNode as JavaScriptWithScopeNode).scope
    }

    override fun createJavaScriptWithScopeNode(scriptContent: String, scopeDocumentNode: BsonNode): BsonNode {
        return JavaScriptWithScopeNode(scriptContent, scopeDocumentNode as DocumentNode)
    }

    override fun getInt32NodeValue(int32Node: BsonNode): Int {
        return (int32Node as Int32Node).value
    }

    override fun createInt32Node(value: Int): BsonNode {
        return Int32Node(value)
    }

    override fun getInt64NodeValue(int64Node: BsonNode): Long {
        return (int64Node as Int64Node).value
    }

    override fun createInt64Node(value: Long): BsonNode {
        return Int64Node(value)
    }


    override fun getTimestampNodeValue(timestampNode: BsonNode): Long {
        return (timestampNode as TimestampNode).value
    }

    override fun createTimestampNode(value: Long): BsonNode {
        return TimestampNode(value)
    }

    override fun getDecimal128NodeValue(decimal128Node: BsonNode): ByteArray {
        return (decimal128Node as Decimal128Node).value
    }

    override fun createDecimal128Node(value: ByteArray): BsonNode {
        return Decimal128Node(value)
    }

    override fun createMinKeyNode(): BsonNode {
        return MinKeyNode
    }

    override fun createMaxKeyNode(): BsonNode {
        return MaxKeyNode
    }

    override fun getSymbolNodeValue(symbolNode: BsonNode): String {
        return (symbolNode as SymbolNode).value
    }

    override fun createSymbolNode(value: String): BsonNode {
        return SymbolNode(value)
    }


}