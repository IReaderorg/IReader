package eu.kanade.tachiyomi.network

/**
 * Minimal HttpException shim for tsundoku extension compatibility.
 */
class HttpException(val code: Int) : IllegalStateException("HTTP error $code")
