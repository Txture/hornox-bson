package io.txture.hornoxbson.exception

import java.io.IOException

open class HornoxException: IOException {

    constructor() : super()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

}