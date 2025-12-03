package ireader.core.http

import platform.JavaScriptCore.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of JS engine using JavaScriptCore framework
 * 
 * JavaScriptCore is Apple's built-in JavaScript engine, available on all iOS devices.
 * It provides fast, secure JavaScript execution without needing a WebView.
 */
@OptIn(ExperimentalForeignApi::class)
actual class JS {
    
    private var context: JSContext? = JSContext()
    
    init {
        // Set up exception handler
        context?.exceptionHandler = { _, exception ->
            println("[JS Error] ${exception?.toString() ?: "Unknown error"}")
        }
    }
    
    /**
     * Evaluates the given JavaScript [script] and returns its result as [String]
     */
    actual fun evaluateAsString(script: String): String {
        val ctx = context ?: throw IllegalStateException("JSContext has been closed")
        val result = ctx.evaluateScript(script)
        
        // Check for exceptions
        ctx.exception?.let { exception ->
            val errorMsg = exception.toString()
            ctx.exception = null
            throw RuntimeException("JavaScript error: $errorMsg")
        }
        
        return result?.toString() ?: ""
    }
    
    /**
     * Evaluates the given JavaScript [script] and returns its result as [Int]
     */
    actual fun evaluateAsInt(script: String): Int {
        val ctx = context ?: throw IllegalStateException("JSContext has been closed")
        val result = ctx.evaluateScript(script)
        
        // Check for exceptions
        ctx.exception?.let { exception ->
            val errorMsg = exception.toString()
            ctx.exception = null
            throw RuntimeException("JavaScript error: $errorMsg")
        }
        
        return result?.toInt32() ?: 0
    }
    
    /**
     * Evaluates the given JavaScript [script] and returns its result as [Double]
     */
    actual fun evaluateAsDouble(script: String): Double {
        val ctx = context ?: throw IllegalStateException("JSContext has been closed")
        val result = ctx.evaluateScript(script)
        
        // Check for exceptions
        ctx.exception?.let { exception ->
            val errorMsg = exception.toString()
            ctx.exception = null
            throw RuntimeException("JavaScript error: $errorMsg")
        }
        
        return result?.toDouble() ?: 0.0
    }
    
    /**
     * Evaluates the given JavaScript [script] and returns its result as [Boolean]
     */
    actual fun evaluateAsBoolean(script: String): Boolean {
        val ctx = context ?: throw IllegalStateException("JSContext has been closed")
        val result = ctx.evaluateScript(script)
        
        // Check for exceptions
        ctx.exception?.let { exception ->
            val errorMsg = exception.toString()
            ctx.exception = null
            throw RuntimeException("JavaScript error: $errorMsg")
        }
        
        return result?.toBool() ?: false
    }
    
    /**
     * Closes this instance. No evaluations can be made after calling this method.
     */
    actual fun close() {
        context = null
    }
}
