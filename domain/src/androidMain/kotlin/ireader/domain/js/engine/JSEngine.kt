package ireader.domain.js.engine

import app.cash.quickjs.QuickJs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of JSEngine using QuickJS.
 */
actual class JSEngine {
    
    private var quickJs: QuickJs? = null
    private val memoryLimit = 64 * 1024 * 1024L // 64MB
    
    actual fun initialize() {
        if (quickJs != null) {
            return
        }
        
        try {
            quickJs = QuickJs.create()
            // Note: QuickJS Android library doesn't expose memory limit API directly
            // Memory limits would need to be enforced at a higher level
            
            // Apply sandbox restrictions
            applySandboxRestrictions()
        } catch (e: Exception) {
            throw JSException("Failed to initialize QuickJS engine", cause = e)
        }
    }
    
    /**
     * Applies sandbox restrictions to prevent dangerous operations.
     */
    private fun applySandboxRestrictions() {
        val engine = quickJs ?: return
        
        try {
            // Disable eval() by overriding it
            engine.evaluate("globalThis.eval = undefined;")
            
            // Note: Function constructor is allowed for transpiled code compatibility
            // The sandbox environment prevents actual harm
            
            // Setup CommonJS-like module system for LNReader plugin compatibility
            engine.evaluate("""
                // Create module and exports objects for CommonJS compatibility
                if (typeof module === 'undefined') {
                    globalThis.module = { exports: {} };
                }
                if (typeof exports === 'undefined') {
                    globalThis.exports = globalThis.module.exports;
                }
                
                // Disable other potentially dangerous globals
                globalThis.importScripts = undefined;
                globalThis.WebAssembly = undefined;
            """.trimIndent())
        } catch (e: Exception) {
            // Log but don't fail initialization if sandbox restrictions fail
            // This is a best-effort security measure
        }
    }
    
    actual fun evaluateScript(script: String): Any? {
        val engine = quickJs ?: throw JSException("Engine not initialized")
        
        return try {
            engine.evaluate(script)
        } catch (e: Exception) {
            throw JSException(
                "Script evaluation failed: ${e.message}",
                jsStackTrace = e.stackTraceToString(),
                cause = e
            )
        }
    }
    
    actual fun callFunction(name: String, vararg args: Any?): Any? {
        val engine = quickJs ?: throw JSException("Engine not initialized")
        
        return try {
            // Build a JavaScript call expression
            val argsJson = args.joinToString(", ") { arg ->
                when (arg) {
                    null -> "null"
                    is String -> "\"${arg.replace("\"", "\\\"")}\""
                    is Number -> arg.toString()
                    is Boolean -> arg.toString()
                    is Map<*, *> -> convertMapToJson(arg)
                    is List<*> -> convertListToJson(arg)
                    else -> "\"${arg.toString().replace("\"", "\\\"")}\""
                }
            }
            
            val callScript = "$name($argsJson)"
            engine.evaluate(callScript)
        } catch (e: Exception) {
            throw JSException(
                "Function call failed: $name - ${e.message}",
                jsStackTrace = e.stackTraceToString(),
                cause = e
            )
        }
    }
    
    actual fun setGlobalObject(name: String, value: Any) {
        val engine = quickJs ?: throw JSException("Engine not initialized")
        
        try {
            // For complex objects, we need to set them via script evaluation
            when (value) {
                is String -> engine.evaluate("globalThis.$name = \"${value.replace("\"", "\\\"")}\"")
                is Number -> engine.evaluate("globalThis.$name = $value")
                is Boolean -> engine.evaluate("globalThis.$name = $value")
                else -> {
                    // For complex objects, we store a reference and create a proxy
                    globalObjects[name] = value
                    engine.evaluate("globalThis.$name = { __nativeObject: '$name' }")
                }
            }
        } catch (e: Exception) {
            throw JSException("Failed to set global object: $name", cause = e)
        }
    }
    
    actual fun getGlobalObject(name: String): Any? {
        val engine = quickJs ?: throw JSException("Engine not initialized")
        
        return try {
            engine.evaluate("globalThis.$name")
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun dispose() {
        quickJs?.close()
        quickJs = null
        globalObjects.clear()
    }
    
    private fun convertMapToJson(map: Map<*, *>): String {
        val entries = map.entries.joinToString(", ") { (key, value) ->
            val keyStr = key.toString()
            val valueStr = when (value) {
                null -> "null"
                is String -> "\"${value.replace("\"", "\\\"")}\""
                is Number -> value.toString()
                is Boolean -> value.toString()
                is Map<*, *> -> convertMapToJson(value)
                is List<*> -> convertListToJson(value)
                else -> "\"${value.toString().replace("\"", "\\\"")}\""
            }
            "\"$keyStr\": $valueStr"
        }
        return "{$entries}"
    }
    
    private fun convertListToJson(list: List<*>): String {
        val items = list.joinToString(", ") { item ->
            when (item) {
                null -> "null"
                is String -> "\"${item.replace("\"", "\\\"")}\""
                is Number -> item.toString()
                is Boolean -> item.toString()
                is Map<*, *> -> convertMapToJson(item)
                is List<*> -> convertListToJson(item)
                else -> "\"${item.toString().replace("\"", "\\\"")}\""
            }
        }
        return "[$items]"
    }
    
    companion object {
        // Store native objects that are exposed to JavaScript
        private val globalObjects = mutableMapOf<String, Any>()
        
        fun getNativeObject(name: String): Any? {
            return globalObjects[name]
        }
    }
}
