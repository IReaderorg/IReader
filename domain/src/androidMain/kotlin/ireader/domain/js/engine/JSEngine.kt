package ireader.domain.js.engine

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.utils.V8ObjectUtils

/**
 * Android implementation of JSEngine using J2V8 (V8 JavaScript engine).
 * 
 * V8 provides full support for modern JavaScript including:
 * - Native Promise support (no polling needed!)
 * - async/await
 * - Arrow functions
 * - const/let
 * - Template literals
 * - Destructuring
 * - Classes
 * - Generators
 * 
 * This is the same engine that powers Chrome and Node.js!
 */
actual class JSEngine {
    
    private var v8: V8? = null
    
    actual fun initialize() {
        if (v8 != null) {
            return
        }
        
        try {
            // Create V8 runtime
            v8 = V8.createV8Runtime()
            
            // Setup CommonJS-like module system
            v8?.executeScript("""
                if (typeof module === 'undefined') {
                    var module = { exports: {} };
                }
                if (typeof exports === 'undefined') {
                    var exports = module.exports;
                }
                
                // Disable potentially dangerous globals
                var importScripts = undefined;
                var WebAssembly = undefined;
            """.trimIndent())
        } catch (e: Exception) {
            v8?.release()
            v8 = null
            throw JSException("Failed to initialize V8 engine", cause = e)
        }
    }
    
    actual fun evaluateScript(script: String): Any? {
        val engine = v8 ?: throw JSException("Engine not initialized")
        
        return try {
            val result = engine.executeScript(script)
            convertToKotlin(result)
        } catch (e: Exception) {
            throw JSException(
                "Script evaluation failed: ${e.message}",
                jsStackTrace = e.stackTraceToString(),
                cause = e
            )
        }
    }
    
    actual fun callFunction(name: String, vararg args: Any?): Any? {
        val engine = v8 ?: throw JSException("Engine not initialized")
        
        return try {
            // Get the function
            val func = engine.getObject(name)
            if (func == null || func !is com.eclipsesource.v8.V8Function) {
                throw JSException("$name is not a function")
            }
            
            // Convert arguments
            val v8Args = V8Array(engine)
            try {
                args.forEach { arg ->
                    when (arg) {
                        null -> v8Args.pushNull()
                        is String -> v8Args.push(arg)
                        is Int -> v8Args.push(arg)
                        is Double -> v8Args.push(arg)
                        is Boolean -> v8Args.push(arg)
                        else -> v8Args.push(arg.toString())
                    }
                }
                
                // Call function
                val result = func.call(engine, v8Args)
                convertToKotlin(result)
            } finally {
                v8Args.release()
                func.release()
            }
        } catch (e: Exception) {
            throw JSException(
                "Function call failed: $name - ${e.message}",
                jsStackTrace = e.stackTraceToString(),
                cause = e
            )
        }
    }
    
    actual fun setGlobalObject(name: String, value: Any) {
        val engine = v8 ?: throw JSException("Engine not initialized")
        
        try {
            when (value) {
                is String -> engine.add(name, value)
                is Int -> engine.add(name, value)
                is Double -> engine.add(name, value)
                is Boolean -> engine.add(name, value)
                else -> engine.add(name, value.toString())
            }
        } catch (e: Exception) {
            throw JSException("Failed to set global object: $name", cause = e)
        }
    }
    
    actual fun getGlobalObject(name: String): Any? {
        val engine = v8 ?: throw JSException("Engine not initialized")
        
        return try {
            val result = engine.get(name)
            convertToKotlin(result)
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun dispose() {
        try {
            v8?.release()
        } catch (e: Exception) {
            // Ignore
        }
        v8 = null
    }
    
    /**
     * Convert V8 value to Kotlin value.
     */
    private fun convertToKotlin(value: Any?): Any? {
        return when (value) {
            null, V8.getUndefined() -> null
            is String, is Number, is Boolean -> value
            is V8Array -> {
                val list = mutableListOf<Any?>()
                try {
                    for (i in 0 until value.length()) {
                        list.add(convertToKotlin(value.get(i)))
                    }
                    list
                } finally {
                    value.release()
                }
            }
            is V8Object -> {
                // For now, return as-is (can be enhanced later)
                value
            }
            else -> value
        }
    }
}
