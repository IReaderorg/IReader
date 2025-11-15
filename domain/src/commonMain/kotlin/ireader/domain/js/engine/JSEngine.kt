package ireader.domain.js.engine

import kotlinx.coroutines.withTimeout

/**
 * JavaScript engine interface for executing JavaScript code.
 * Platform-specific implementations use QuickJS (Android) or GraalVM (Desktop).
 */
expect class JSEngine() {
    
    /**
     * Initializes the JavaScript engine.
     * Must be called before any other operations.
     */
    fun initialize()
    
    /**
     * Evaluates a JavaScript script and returns the result.
     * @param script The JavaScript code to execute
     * @return The result of the script execution
     * @throws JSException if execution fails
     */
    fun evaluateScript(script: String): Any?
    
    /**
     * Calls a JavaScript function by name.
     * @param name The function name
     * @param args The function arguments
     * @return The function result
     * @throws JSException if the call fails
     */
    fun callFunction(name: String, vararg args: Any?): Any?
    
    /**
     * Sets a global object in the JavaScript context.
     * @param name The global variable name
     * @param value The value to set
     */
    fun setGlobalObject(name: String, value: Any)
    
    /**
     * Gets a global object from the JavaScript context.
     * @param name The global variable name
     * @return The global object value, or null if not found
     */
    fun getGlobalObject(name: String): Any?
    
    /**
     * Disposes the JavaScript engine and releases resources.
     * The engine cannot be used after disposal.
     */
    fun dispose()
}

/**
 * Executes a JavaScript operation with a timeout.
 * @param timeoutMillis Timeout in milliseconds (default 30 seconds)
 * @param block The operation to execute
 * @return The result of the operation
 * @throws kotlinx.coroutines.TimeoutCancellationException if timeout is exceeded
 */
suspend fun <T> JSEngine.withTimeout(
    timeoutMillis: Long = 30000L,
    block: suspend () -> T
): T {
    return withTimeout(timeoutMillis) {
        block()
    }
}
