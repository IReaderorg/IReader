package ireader.domain.plugins

import ireader.core.io.VirtualFile

/**
 * iOS implementation of ZIP extraction for plugins
 */
actual suspend fun extractZipEntry(file: VirtualFile, entryName: String): String? {
    // TODO: Implement ZIP extraction
    return null
}

/**
 * iOS implementation of plugin instantiation
 * Not supported on iOS - use JS plugins instead
 */
actual fun instantiatePlugin(pluginClass: Any): Plugin {
    throw UnsupportedOperationException("Native plugin instantiation is not supported on iOS. Use JS plugins instead.")
}
