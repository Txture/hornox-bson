package io.txture.hornoxbson.model

import jakarta.json.*
import io.txture.hornoxbson.JakartaJsonExtensions.toBsonNode

class DocumentNode(
    var length: Int = -1,
    val fields: MutableMap<String, BsonNode> = mutableMapOf(),
) : BsonNode, JsonObject {


    companion object {

        @JvmField
        val FINGERPRINT_BYTE = 0x03.toByte()

    }

    override val fingerprintByte: Byte
        get() = FINGERPRINT_BYTE

    override fun toString(): String {
        return "{${this.fields.entries.joinToString { "\"${it.key}\": ${it.value}" }}}"
    }

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.OBJECT
    }

    override fun clear() {
        this.fields.clear()
    }

    override fun put(key: String, value: JsonValue?): JsonValue? {
        return this.fields.put(key, value.toBsonNode())
    }

    override fun putAll(from: Map<out String, JsonValue?>) {
        for ((key, value) in from) {
            this[key] = value
        }
    }

    override fun remove(key: String?): JsonValue? {
        if (key == null) {
            return null
        }
        return this.fields.remove(key) ?: NullNode
    }

    override fun containsKey(key: String?): Boolean {
        if (key == null) {
            return false
        }
        return this.fields.containsKey(key)
    }

    override fun containsValue(value: JsonValue?): Boolean {
        val realValue = value.toBsonNode()
        return this.fields.containsValue(realValue)
    }

    override fun get(key: String?): JsonValue? {
        return this.fields[key]
    }

    override fun isEmpty(): Boolean {
        return this.fields.isEmpty()
    }

    override fun getJsonArray(name: String): JsonArray {
        return when (val node = this.fields[name]) {
            is ArrayNode -> node
            null -> throw NullPointerException("Cannot get key '${name}' as JsonArray - there is no value for this key!")
            else -> throw ClassCastException("Cannot get key '${name}' as JsonArray - it is of type ${node.javaClass.name}!")
        }
    }

    override fun getJsonObject(name: String): JsonObject {
        return when (val node = this.fields[name]) {
            is DocumentNode -> node
            null -> throw NullPointerException("Cannot get key '${name}' as JsonObject - there is no value for this key!")
            else -> throw ClassCastException("Cannot get key '${name}' as JsonObject - it is of type ${node.javaClass.name}!")
        }
    }

    override fun getJsonNumber(name: String): JsonNumber {
        return when (val node = this.fields[name]) {
            is JsonNumber -> node
            null -> throw NullPointerException("Cannot get key '${name}' as JsonNumber - there is no value for this key!")
            else -> throw ClassCastException("Cannot get key '${name}' as JsonNumber - it is of type ${node.javaClass.name}!")
        }
    }

    override fun getJsonString(name: String): JsonString {
        return when (val node = this.fields[name]) {
            is JsonString -> node
            null -> throw NullPointerException("Cannot get key '${name}' as JsonString - there is no value for this key!")
            else -> throw ClassCastException("Cannot get key '${name}' as JsonString - it is of type ${node.javaClass.name}!")
        }
    }

    override fun getBoolean(name: String): Boolean {
        return when (val node = this.fields[name]) {
            is BooleanNode -> node.value
            null -> throw NullPointerException("Cannot get key '${name}' as Boolean - there is no value for this key!")
            else -> throw ClassCastException("Cannot get key '${name}' as Boolean - the node is of type ${node.javaClass.name}!")
        }
    }

    override fun getBoolean(name: String, defaultValue: Boolean): Boolean {
        return when (val node = this.fields[name]) {
            is BooleanNode -> node.value
            else -> defaultValue
        }
    }

    override fun getInt(name: String): Int {
        return when (val node = this.fields[name]) {
            is Int32Node -> node.value
            null -> throw NullPointerException("Cannot get key '${name}' as Int - there is no value for this key!")
            else -> throw ClassCastException("Cannot get key '${name}' as Int - the node is of type ${node.javaClass.name}!")
        }
    }

    override fun getInt(name: String, defaultValue: Int): Int {
        return when (val node = this.fields[name]) {
            is Int32Node -> node.value
            else -> defaultValue
        }
    }

    override fun getString(name: String?): String {
        return when (val node = this.fields[name]) {
            is TextNode -> node.value
            null -> throw NullPointerException("Cannot get key '${name}' as Int - there is no value for this key!")
            else -> throw ClassCastException("Cannot get key '${name}' as Int - the node is of type ${node.javaClass.name}!")
        }
    }

    override fun getString(name: String, defaultValue: String): String {
        return when (val node = this.fields[name]) {
            is TextNode -> node.value
            else -> defaultValue
        }
    }

    override fun isNull(name: String): Boolean {
        return this.fields[name] is NullNode
    }

    @Suppress("UNCHECKED_CAST")
    override val entries: MutableSet<MutableMap.MutableEntry<String, JsonValue>>
        get() = EntriesCollection()

    override val keys: MutableSet<String>
        get() = this.fields.keys

    override val size: Int
        get() = this.fields.size

    override val values: MutableCollection<JsonValue>
        get() = this.fields.values as MutableCollection<JsonValue>


    inner class EntriesCollection: MutableSet<MutableMap.MutableEntry<String, JsonValue>> {
        override fun add(element: MutableMap.MutableEntry<String, JsonValue>): Boolean {
            if(this@DocumentNode.containsKey(element.key)){
                return false
            }
            this@DocumentNode[element.key] = element.value.toBsonNode()
            return true
        }

        override fun addAll(elements: Collection<MutableMap.MutableEntry<String, JsonValue>>): Boolean {
            var changed = false
            for(entry in elements){
                if(this.add(entry)){
                    changed = true
                }
            }
            return changed
        }

        override val size: Int
            get() = this@DocumentNode.size

        override fun clear() {
            this@DocumentNode.clear()
        }

        override fun isEmpty(): Boolean {
            return this@DocumentNode.isEmpty()
        }

        override fun containsAll(elements: Collection<MutableMap.MutableEntry<String, JsonValue>>): Boolean {
            for(element in elements){
                if(!this.contains(element)){
                    return false
                }
            }
            return true
        }

        override fun contains(element: MutableMap.MutableEntry<String, JsonValue>): Boolean {
            val actualValue = this@DocumentNode[element.key]
                ?: return false
            return actualValue == element.value.toBsonNode()
        }

        @Suppress("UNCHECKED_CAST")
        override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, JsonValue>> {
            return this@DocumentNode.fields.iterator() as MutableIterator<MutableMap.MutableEntry<String, JsonValue>>
        }

        override fun retainAll(elements: Collection<MutableMap.MutableEntry<String, JsonValue>>): Boolean {
            val map = elements.asSequence().map { it.key to it.value }.toMap()
            val iterator = this.iterator()
            var changed = false
            while(iterator.hasNext()){
                val current = iterator.next()
                if(!map.containsKey(current.key)){
                    iterator.remove()
                    changed = true
                    continue
                }
                val expectedValue = map[current.key]
                if(current.value != expectedValue.toBsonNode()){
                    iterator.remove()
                    changed = true
                    continue
                }
            }
            return changed
        }

        override fun removeAll(elements: Collection<MutableMap.MutableEntry<String, JsonValue>>): Boolean {
            var changed = false
            for(entry in elements){
                if(this.remove(entry)){
                    changed = true
                }
            }
            return changed
        }

        override fun remove(element: MutableMap.MutableEntry<String, JsonValue>): Boolean {
            val currentValue = this@DocumentNode[element.key]
            if(currentValue != element.value.toBsonNode()){
                return false
            }
            this@DocumentNode.remove(element.key)
            return true
        }

    }

}