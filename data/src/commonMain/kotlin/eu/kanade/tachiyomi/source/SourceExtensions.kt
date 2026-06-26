package eu.kanade.tachiyomi.source

/**
 * Shim for tsundoku's isNovelSource extension on Source.
 * Uses reflection since Source is a tsundoku type not available at compile time.
 */
fun isNovelSource(source: Any): Boolean {
    return try {
        val method = source.javaClass.getMethod("isNovelSource")
        method.invoke(source) as? Boolean ?: false
    } catch (e: Exception) {
        false
    }
}
