package io.txture.hornoxbson.model

import jakarta.json.JsonValue

sealed interface BsonNode : JsonValue {

    val fingerprintByte: Byte
        get() = this.nodeType.fingerprintByte

    val nodeType: NodeType

}