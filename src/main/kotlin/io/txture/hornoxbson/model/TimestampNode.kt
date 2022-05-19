package io.txture.hornoxbson.model

class TimestampNode(
    override val value: Long,
) : LongNode {

    override val nodeType: NodeType
        get() = NodeType.TIMESTAMP


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as TimestampNode

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "TS[${this.value}]"
    }


}