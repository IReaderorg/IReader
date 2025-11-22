package ireader.domain.js.engine

/**
 * Exception thrown when a plugin fails to load.
 */
class PluginLoadException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
