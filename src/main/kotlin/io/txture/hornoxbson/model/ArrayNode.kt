package io.txture.hornoxbson.model

import jakarta.json.*
import io.txture.hornoxbson.JakartaJsonExtensions.toBsonNode

class ArrayNode(
    var length: Int = -1,
    val fields: MutableList<BsonNode> = mutableListOf(),
) : BsonNode, JsonArray {

    companion object {

        @JvmField
        val FINGERPRINT_BYTE = 0x04.toByte()

    }

    constructor(vararg nodes: BsonNode): this(length = -1, fields = nodes.toMutableList())

    override val fingerprintByte: Byte
        get() = FINGERPRINT_BYTE

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.ARRAY
    }

    override fun toString(): String {
        return "[${this.fields.joinToString()}]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ArrayNode

        if (fields != other.fields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + fields.hashCode()
        return result
    }

    override fun add(element: JsonValue): Boolean {
        return this.fields.add(element.toBsonNode())
    }

    override fun add(index: Int, element: JsonValue) {
        return this.fields.add(index, element.toBsonNode())
    }

    override fun addAll(elements: Collection<JsonValue>): Boolean {
        return this.fields.addAll(elements.map { it.toBsonNode() })
    }

    override fun addAll(index: Int, elements: Collection<JsonValue>): Boolean {
        return this.fields.addAll(index, elements.map { it.toBsonNode() })
    }

    override fun clear() {
        this.fields.clear()
    }

    override fun contains(element: JsonValue): Boolean {
        return this.fields.contains(element.toBsonNode())
    }

    override fun containsAll(elements: Collection<JsonValue>): Boolean {
        return this.fields.containsAll(elements.map { it.toBsonNode() })
    }

    override fun get(index: Int): JsonValue {
        return this.fields.get(index)
    }

    override fun isEmpty(): Boolean {
        return this.fields.isEmpty()
    }

    override fun indexOf(element: JsonValue): Int {
        return this.fields.indexOf(element.toBsonNode())
    }

    override fun lastIndexOf(element: JsonValue): Int {
        return this.fields.lastIndexOf(element.toBsonNode())
    }

    override fun listIterator(index: Int): MutableListIterator<JsonValue> {
        return BsonListIterator(this.fields.listIterator(index))
    }

    override val size: Int
        get() = this.fields.size

    override fun set(index: Int, element: JsonValue): JsonValue {
        return this.fields.set(index, element.toBsonNode())
    }

    override fun removeAt(index: Int): JsonValue {
        return this.fields.removeAt(index)
    }

    override fun iterator(): MutableIterator<JsonValue> {
        return this.listIterator()
    }

    override fun listIterator(): MutableListIterator<JsonValue> {
        return BsonListIterator(this.fields.listIterator())
    }

    override fun getBoolean(index: Int): Boolean {
        return when (val element = this@ArrayNode[index]) {
            is BooleanNode -> element.value
            else -> throw ClassCastException("Cannot get array element at index '${index}' as Boolean - its node is of type ${element.javaClass.name}!")
        }
    }

    override fun getBoolean(index: Int, defaultValue: Boolean): Boolean {
        return when (val element = this@ArrayNode[index]) {
            is BooleanNode -> element.value
            else -> defaultValue
        }
    }

    override fun getInt(index: Int): Int {
        return when (val element = this@ArrayNode[index]) {
            is Int32Node -> element.value
            else -> throw ClassCastException("Cannot get array element at index '${index}' as Integer - its node is of type ${element.javaClass.name}!")
        }
    }

    override fun getInt(index: Int, defaultValue: Int): Int {
        return when (val element = this@ArrayNode[index]) {
            is Int32Node -> element.value
            else -> defaultValue
        }
    }

    override fun getString(index: Int): String {
        return when (val element = this@ArrayNode[index]) {
            is TextNode -> element.value
            else -> throw ClassCastException("Cannot get array element at index '${index}' as String - its node is of type ${element.javaClass.name}!")
        }
    }

    override fun getString(index: Int, defaultValue: String): String {
        return when (val element = this@ArrayNode[index]) {
            is TextNode -> element.value
            else -> defaultValue
        }
    }

    override fun getJsonNumber(index: Int): JsonNumber {
        return when (val element = this@ArrayNode[index]) {
            is JsonNumber -> element
            else -> throw ClassCastException("Cannot get array element at index '${index}' as String - its node is of type ${element.javaClass.name}!")
        }
    }

    override fun getJsonString(index: Int): JsonString {
        return when (val element = this@ArrayNode[index]) {
            is TextNode -> element
            else -> throw ClassCastException("Cannot get array element at index '${index}' as String - its node is of type ${element.javaClass.name}!")
        }
    }

    override fun getJsonArray(index: Int): JsonArray {
        return when (val element = this@ArrayNode[index]) {
            is JsonArray -> element
            else -> throw ClassCastException("Cannot get array element at index '${index}' as JsonArray - its node is of type ${element.javaClass.name}!")
        }
    }

    override fun getJsonObject(index: Int): JsonObject {
        return when (val element = this@ArrayNode[index]) {
            is JsonObject -> element
            else -> throw ClassCastException("Cannot get array element at index '${index}' as JsonObject - its node is of type ${element.javaClass.name}!")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : JsonValue> getValuesAs(clazz: Class<T>): MutableList<T> {
        return this.fields.asSequence().map {clazz.cast(it)}.toMutableList()
    }

    override fun isNull(index: Int): Boolean {
        return this.fields[index] is NullNode
    }

    override fun removeAll(elements: Collection<JsonValue>): Boolean {
        return this.fields.removeAll(elements.map { it.toBsonNode() })
    }

    override fun remove(element: JsonValue): Boolean {
        return this.fields.remove(element.toBsonNode())
    }

    override fun retainAll(elements: Collection<JsonValue>): Boolean {
        return this.fields.retainAll(elements.map { it.toBsonNode() })
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<JsonValue> {
        return BsonSubListView(this.fields.subList(fromIndex, toIndex))
    }

    @Suppress("UNCHECKED_CAST")
    private class BsonListIterator(
        private val innerIterator: MutableListIterator<BsonNode>
    ) : MutableListIterator<JsonValue> by innerIterator as MutableListIterator<JsonValue> {

        override fun add(element: JsonValue) {
            this.innerIterator.add(element.toBsonNode())
        }

        override fun set(element: JsonValue) {
            this.innerIterator.set(element.toBsonNode())
        }

    }

    private class BsonSubListView(
        private val innerList: MutableList<BsonNode>
    ): MutableList<JsonValue> {

        override fun add(element: JsonValue): Boolean {
            return this.innerList.add(element.toBsonNode())
        }

        override fun addAll(elements: Collection<JsonValue>): Boolean {
            return this.innerList.addAll(elements.map { it.toBsonNode() })
        }

        override fun clear() {
            this.innerList.clear()
        }

        override val size: Int
            get() = this.innerList.size

        override fun isEmpty(): Boolean {
            return this.innerList.isEmpty()
        }

        override fun removeAll(elements: Collection<JsonValue>): Boolean {
            return this.innerList.removeAll(elements.map { it.toBsonNode() })
        }

        override fun remove(element: JsonValue): Boolean {
            return this.innerList.remove(element.toBsonNode())
        }

        override fun retainAll(elements: Collection<JsonValue>): Boolean {
            return this.innerList.retainAll(elements.map { it.toBsonNode() })
        }

        override fun addAll(index: Int, elements: Collection<JsonValue>): Boolean {
            return this.innerList.addAll(index, elements.map { it.toBsonNode() })
        }

        override fun add(index: Int, element: JsonValue) {
            this.innerList.add(index, element.toBsonNode())
        }

        override fun listIterator(): MutableListIterator<JsonValue> {
            return BsonListIterator(this.innerList.listIterator())
        }

        override fun iterator(): MutableIterator<JsonValue> {
            return this.listIterator()
        }

        override fun lastIndexOf(element: JsonValue): Int {
            return this.innerList.lastIndexOf(element.toBsonNode())
        }

        override fun indexOf(element: JsonValue): Int {
            return this.innerList.indexOf(element.toBsonNode())
        }

        override fun removeAt(index: Int): JsonValue {
            return this.innerList.removeAt(index)
        }

        override fun get(index: Int): JsonValue {
            return this.innerList.get(index)
        }

        override fun containsAll(elements: Collection<JsonValue>): Boolean {
            return this.innerList.containsAll(elements.map { it.toBsonNode() })
        }

        override fun contains(element: JsonValue): Boolean {
            return this.innerList.contains(element.toBsonNode())
        }

        override fun listIterator(index: Int): MutableListIterator<JsonValue> {
            return BsonListIterator(this.innerList.listIterator(index))
        }

        override fun subList(fromIndex: Int, toIndex: Int): MutableList<JsonValue> {
            return BsonSubListView(this.innerList.subList(fromIndex, toIndex))
        }

        override fun set(index: Int, element: JsonValue): JsonValue {
            return this.innerList.set(index, element.toBsonNode())
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if(other is List<*>){
                return false
            }
            return this.innerList == other
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + innerList.hashCode()
            return result
        }


    }

}