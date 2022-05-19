package io.txture.hornoxbson.model

class Int64Node(
    override val value: Long,
) : LongNode {

    override val nodeType: NodeType
        get() = NodeType.INT64

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Int64Node

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return this.value.toString() + "L"
    }

}