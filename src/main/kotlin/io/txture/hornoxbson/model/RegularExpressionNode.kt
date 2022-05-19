package io.txture.hornoxbson.model

import jakarta.json.JsonValue

class RegularExpressionNode : BsonValueNode<String> {

    companion object {

        val allowedCharacters = setOf(
            'i', // case-insensitive
            'm', // multi-line
            'x', // verbose mode
            'l', // make \w, \W etc. locale dependent
            's', // dotall mode ('.' matches everything)
            'u', // make \w,\W etc. match unicode
        )

    }

    override val value: String

    /**
     * Contains the options of this regular expression.
     *
     * In accordance with the BSON specification, the following option characters are supported:
     *
     * - `'i' // case-insensitive`
     * - `'m' // multi-line`
     * - `'x' // verbose mode`
     * - `'l' // make \w, \W etc. locale dependent`
     * - `'s' // dotall mode ('.' matches everything)`
     * - `'u' // make \w,\W etc. match unicode`
     *
     * Any characters which are **not** in this set will be **silently ignored**. Duplicates will
     * also be eliminated, and the resulting options string will always be sorted alphabetically.
     */
    val options: String

    /**
     * Alias for the generic [value] property.
     */
    val regularExpression: String
        get() {
            return this.value
        }

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(regex: String, options: String) {
        this.value = regex
        this.options = options.asSequence().filter { it in allowedCharacters }.distinct().sorted().joinToString(separator = "")
    }

    override val nodeType: NodeType
        get() = NodeType.REGULAR_EXPRESSION

    override fun getValueType(): JsonValue.ValueType {
        return JsonValue.ValueType.STRING
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegularExpressionNode

        if (value != other.value) return false
        if (options != other.options) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + options.hashCode()
        return result
    }

    override fun toString(): String {
        return "REG{exp: '${this.regularExpression}', opt: '${this.options}'}"
    }


}