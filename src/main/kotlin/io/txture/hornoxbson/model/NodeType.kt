package io.txture.hornoxbson.model

enum class NodeType {

    DOUBLE {
        override val fingerprintByte: Byte
            get() = 0x01

    },

    TEXT {
        override val fingerprintByte: Byte
            get() = 0x02
    },


    DOCUMENT {
        override val fingerprintByte: Byte
            get() = 0x03
    },

    ARRAY {
        override val fingerprintByte: Byte
            get() = 0x04
    },


    BINARY {
        override val fingerprintByte: Byte
            get() = 0x05
    },


    UNDEFINED {
        override val fingerprintByte: Byte
            get() = 0x06
    },


    OBJECT_ID {
        override val fingerprintByte: Byte
            get() = 0x07
    },

    TRUE {
        override val fingerprintByte: Byte
            get() = 0x08
    },

    FALSE {
        override val fingerprintByte: Byte
            get() = 0x08
    },

    UTC_DATE_TIME {
        override val fingerprintByte: Byte
            get() = 0x09
    },

    NULL {
        override val fingerprintByte: Byte
            get() = 0x0A
    },

    REGULAR_EXPRESSION {
        override val fingerprintByte: Byte
            get() = 0x0B
    },

    DB_POINTER {
        override val fingerprintByte: Byte
            get() = 0x0C
    },

    JAVA_SCRIPT {
        override val fingerprintByte: Byte
            get() = 0x0D
    },

    SYMBOL {
        override val fingerprintByte: Byte
            get() = 0x0E
    },

    JAVA_SCRIPT_WITH_SCOPE {
        override val fingerprintByte: Byte
            get() = 0x0F
    },

    INT32 {
        override val fingerprintByte: Byte
            get() = 0x10
    },


    TIMESTAMP {
        override val fingerprintByte: Byte
            get() = 0x11
    },

    INT64 {
        override val fingerprintByte: Byte
            get() = 0x12
    },


    DECIMAL_128 {
        override val fingerprintByte: Byte
            get() = 0x13
    },

    MIN_KEY {
        override val fingerprintByte: Byte
            get() = 0xFF.toByte()
    },


    MAX_KEY {
        override val fingerprintByte: Byte
            get() = 0x7F
    },
    ;

    abstract val fingerprintByte: Byte

}