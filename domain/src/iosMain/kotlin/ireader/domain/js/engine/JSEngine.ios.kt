package ireader.domain.js.engine

import kotlinx.cinterop.ExperimentalForeignApi
import platform.JavaScriptCore.JSContext
import platform.JavaScriptCore.objectAtIndexedSubscript
import platform.JavaScriptCore.objectForKeyedSubscript
import platform.JavaScriptCore.setObject
import platform.darwin.NSObject

/**
 * iOS implementation of JSEngine using JavaScriptCore framework
 */
@OptIn(ExperimentalForeignApi::class)
actual class JSEngine actual constructor() {
    
    private var context: JSContext? = null
    
    actual fun initialize() {
        if (context != null) return
        
        context = JSContext().apply {
            exceptionHandler = { _, exception ->
                println("[JSEngine Error] ${exception?.toString() ?: "Unknown error"}")
            }
        }
    }
    
    actual fun evaluateScript(script: String): Any? {
        val ctx = context ?: run {
            initialize()
            context
        } ?: return null
        
        val result = ctx.evaluateScript(script)
        
        ctx.exception?.let { exception ->
            val errorMsg = exception.toString()
            ctx.exception = null
            throw RuntimeException("JavaScript error: $errorMsg")
        }
        
        return convertJSValueToKotlin(result)
    }
    
    actual fun callFunction(name: String, vararg args: Any?): Any? {
        val ctx = context ?: return null
        
        val function = ctx.objectForKeyedSubscript(name)
        if (function == null || function.isUndefined()) {
            return null
        }
        
        val result = function.callWithArguments(args.toList())
        
        ctx.exception?.let { exception ->
            val errorMsg = exception.toString()
            ctx.exception = null
            throw RuntimeException("JavaScript error calling $name: $errorMsg")
        }
        
        return convertJSValueToKotlin(result)
    }
    
    actual fun setGlobalObject(name: String, value: Any) {
        val ctx = context ?: run {
            initialize()
            context
        } ?: return
        
        // Convert name to NSObject for setObject
        ctx.setObject(value, forKeyedSubscript = name as NSObject)
    }
    
    actual fun getGlobalObject(name: String): Any? {
        val ctx = context ?: return null
        val jsValue = ctx.objectForKeyedSubscript(name)
        return convertJSValueToKotlin(jsValue)
    }
    
    actual fun dispose() {
        context = null
    }
    
    private fun convertJSValueToKotlin(jsValue: platform.JavaScriptCore.JSValue?): Any? {
        if (jsValue == null || jsValue.isNull() || jsValue.isUndefined()) {
            return null
        }
        
        return when {
            jsValue.isBoolean() -> jsValue.toBool()
            jsValue.isNumber() -> jsValue.toDouble()
            jsValue.isString() -> jsValue.toString()
            jsValue.isArray() -> {
                val length = jsValue.objectForKeyedSubscript("length")?.toInt32() ?: 0
                (0 until length).map { index ->
                    convertJSValueToKotlin(jsValue.objectAtIndexedSubscript(index.toULong()))
                }
            }
            jsValue.isObject() -> {
                jsValue.toString()
            }
            else -> jsValue.toString()
        }
    }
}
