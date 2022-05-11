package io.txture.hornoxbson.model

sealed interface BsonValueNode<T>: BsonNode {

    val value: T

}