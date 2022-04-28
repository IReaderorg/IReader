

package org.ireader.core_api.http

@Suppress("unused")
class RatelimitException : Exception {

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(cause: Exception) : super(cause)

    constructor(message: String, cause: Exception) : super(message, cause)
}
