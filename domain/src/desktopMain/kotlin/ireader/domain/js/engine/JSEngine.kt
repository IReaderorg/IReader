package ireader.domain.js.engine

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.ResourceLimits
import org.graalvm.polyglot.Value

/**
 * Desktop implementation of JSEngine using GraalVM JavaScript.
 */
actual class JSEngine {
    
    private var context: Context? = null
    private val memoryLimit = 64 * 1024 * 1024L // 64MB
    
    actual fun initialize() {
        if (context != null) {
            return
        }
        
        try {
            // Create resource limits
            val limits = ResourceLimits.newBuilder()
                .statementLimit(10_000_000, null) // Increased limit for complex plugins (10M statements)
                .build()
            
            // Create context with resource limits and host access
            context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup { false } // Disable host class lookup for security
                .resourceLimits(limits)
                .option("js.ecmascript-version", "2022")
                .build()
            
            // Apply sandbox restrictions
            applySandboxRestrictions()
        } catch (e: Exception) {
            throw JSException("Failed to initialize GraalVM context", cause = e)
        }
    }
    
    /**
     * Applies sandbox restrictions to prevent dangerous operations.
     */
    private fun applySandboxRestrictions() {
        val ctx = context ?: return
        
        try {
            // Disable eval() by overriding it
            ctx.eval("js", "globalThis.eval = undefined;")
            
            // Note: Function constructor is allowed for transpiled code compatibility
            // The sandbox environment prevents actual harm
            
            // Setup CommonJS-like module system for LNReader plugin compatibility
            ctx.eval("js", """
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
        val ctx = context ?: throw JSException("Engine not initialized - context is null. The engine may have been disposed.")
        
        return try {
            val result = ctx.eval("js", script)
            convertValue(result)
        } catch (e: Exception) {
            throw JSException(
                "Script evaluation failed: ${e.message}",
                jsStackTrace = e.stackTraceToString(),
                cause = e
            )
        }
    }
    
    actual fun callFunction(name: String, vararg args: Any?): Any? {
        val ctx = context ?: throw JSException("Engine not initialized - context is null. The engine may have been disposed.")
        
        return try {
            val function = ctx.getBindings("js").getMember(name)
            if (function == null || !function.canExecute()) {
                throw JSException("Function not found or not executable: $name")
            }
            
            val convertedArgs = args.map { convertToValue(it) }.toTypedArray()
            val result = function.execute(*convertedArgs)
            convertValue(result)
        } catch (e: JSException) {
            throw e
        } catch (e: Exception) {
            throw JSException(
                "Function call failed: $name - ${e.message}",
                jsStackTrace = e.stackTraceToString(),
                cause = e
            )
        }
    }
    
    actual fun setGlobalObject(name: String, value: Any) {
        val ctx = context ?: throw JSException("Engine not initialized - context is null. The engine may have been disposed.")
        
        try {
            val bindings = ctx.getBindings("js")
            bindings.putMember(name, value)
        } catch (e: Exception) {
            throw JSException("Failed to set global object: $name", cause = e)
        }
    }
    
    actual fun getGlobalObject(name: String): Any? {
        val ctx = context ?: return null
        
        return try {
            val bindings = ctx.getBindings("js")
            val value = bindings.getMember(name)
            convertValue(value)
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun dispose() {
        context?.close()
        context = null
    }
    
    private fun convertValue(value: Value?): Any? {
        if (value == null || value.isNull) return null
        
        return when {
            value.isBoolean -> value.asBoolean()
            value.isString -> value.asString()
            value.fitsInInt() -> value.asInt()
            value.fitsInLong() -> value.asLong()
            value.fitsInDouble() -> value.asDouble()
            value.hasArrayElements() -> {
                val list = mutableListOf<Any?>()
                for (i in 0 until value.arraySize) {
                    list.add(convertValue(value.getArrayElement(i)))
                }
                list
            }
            value.hasMembers() -> {
                val map = mutableMapOf<String, Any?>()
                value.memberKeys.forEach { key ->
                    map[key] = convertValue(value.getMember(key))
                }
                map
            }
            value.isHostObject -> value.asHostObject<Any>()
            else -> value.toString()
        }
    }
    
    private fun convertToValue(obj: Any?): Any? {
        return when (obj) {
            null -> null
            is String, is Number, is Boolean -> obj
            is Map<*, *> -> {
                // Convert map to JavaScript object
                val ctx = context ?: throw JSException("Engine not initialized")
                val jsObj = ctx.eval("js", "({})")
                obj.forEach { (key, value) ->
                    jsObj.putMember(key.toString(), convertToValue(value))
                }
                jsObj
            }
            is List<*> -> {
                // Convert list to JavaScript array
                obj.map { convertToValue(it) }.toTypedArray()
            }
            else -> obj
        }
    }
}
