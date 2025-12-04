package ireader.core.http

/**
 * JavaScript implementation of JS evaluator.
 * 
 * In JS context, we can directly use eval() for JavaScript execution.
 */
actual class JS {
    
    /**
     * Evaluates the given JavaScript script and returns its result as String.
     */
    actual fun evaluateAsString(script: String): String {
        return try {
            val result = js("eval(script)")
            result?.toString() ?: ""
        } catch (e: Exception) {
            throw RuntimeException("JS evaluation failed: ${e.message}")
        }
    }
    
    /**
     * Evaluates the given JavaScript script and returns its result as Int.
     */
    actual fun evaluateAsInt(script: String): Int {
        return try {
            val result = js("eval(script)")
            (result as? Number)?.toInt() ?: result.toString().toInt()
        } catch (e: Exception) {
            throw RuntimeException("JS evaluation failed: ${e.message}")
        }
    }
    
    /**
     * Evaluates the given JavaScript script and returns its result as Double.
     */
    actual fun evaluateAsDouble(script: String): Double {
        return try {
            val result = js("eval(script)")
            (result as? Number)?.toDouble() ?: result.toString().toDouble()
        } catch (e: Exception) {
            throw RuntimeException("JS evaluation failed: ${e.message}")
        }
    }
    
    /**
     * Evaluates the given JavaScript script and returns its result as Boolean.
     */
    actual fun evaluateAsBoolean(script: String): Boolean {
        return try {
            val result = js("eval(script)")
            result as? Boolean ?: result.toString().toBoolean()
        } catch (e: Exception) {
            throw RuntimeException("JS evaluation failed: ${e.message}")
        }
    }
    
    /**
     * Closes this instance.
     * In JS context, this is a no-op.
     */
    actual fun close() {
        // No-op in JS - no resources to clean up
    }
}
