package eu.kanade.tachiyomi.network

/**
 * Minimal ProgressListener shim for tsundoku extension compatibility.
 */
interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}
