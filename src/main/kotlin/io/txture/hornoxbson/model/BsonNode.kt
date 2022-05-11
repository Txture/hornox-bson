package io.txture.hornoxbson.model

import jakarta.json.JsonValue

sealed interface BsonNode: JsonValue {

    val fingerprintByte: Byte

}