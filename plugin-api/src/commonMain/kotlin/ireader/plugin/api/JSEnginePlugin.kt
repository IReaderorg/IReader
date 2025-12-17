package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for JavaScript engines.
 * JS Engine plugins provide JavaScript execution capabilities for running
 * LNReader-compatible source plugins.
 * 
 * This allows the JS engine (GraalVM for Desktop, J2V8 for Android) to be
 * loaded as an optional plugin rather than bundled with the app, reducing
 * the base app size significantly.
 * 
 * Example:
 * ```kotlin
 * class GraalVMEnginePlugin : JSEnginePlugin {
 *     override val manifest = PluginManifest(
 *         id = "io.github.ireaderorg.plugins.graalvm-js",
 *         name = "GraalVM JavaScript Engine",
 *         type = PluginType.JS_ENGINE,
 *         platforms = listOf(Platform.DESKTOP),
 *         nativeLibraries = mapOf(
 *             "windows-x64" to listOf("native/windows-x64/polyglot.dll"),
 *             "macos-arm64" to listOf("native/macos-arm64/libpolyglot.dylib"),
 *             "linux-x64" to listOf("native/linux-x64/libpolyglot.so")
 *         ),
 *         // ...
 *     )
 *     
 *     override fun createEngine(): JSEngineInstance {
 *         return GraalVMEngineInstance()
 *     }
 * }
 * ```
 */
interface JSEnginePlugin : Plugin {
    /**
     * Create a new JavaScript engine instance.
     * Each instance is isolated and can execute JS code independently.
     * 
     * @return A new JS engine instance
     */
    fun createEngine(): JSEngineInstance
    
    /**
     * Get the engine capabilities.
     * 
     * @return Engine capabilities describing supported features
     */
    fun getCapabilities(): JSEngineCapabilities
    
    /**
     * Check if the engine is available and ready to use.
     * This may involve checking if native libraries are loaded.
     * 
     * @return true if the engine is ready
     */
    fun isAvailable(): Boolean
}

/**
 * Instance of a JavaScript engine.
 * Provides methods to execute JavaScript code and interact with JS values.
 */
interface JSEngineInstance {
    /**
     * Initialize the engine instance.
     * Must be called before executing any JavaScript code.
     */
    suspend fun initialize()
    
    /**
     * Execute JavaScript code and return the result.
     * 
     * @param code JavaScript code to execute
     * @return Result of the execution as a JSValue
     */
    suspend fun evaluate(code: String): JSValue
    
    /**
     * Execute JavaScript code with a specific script name (for debugging).
     * 
     * @param code JavaScript code to execute
     * @param scriptName Name of the script (for stack traces)
     * @return Result of the execution as a JSValue
     */
    suspend fun evaluate(code: String, scriptName: String): JSValue
    
    /**
     * Call a JavaScript function by name.
     * 
     * @param functionName Name of the function to call
     * @param args Arguments to pass to the function
     * @return Result of the function call
     */
    suspend fun callFunction(functionName: String, vararg args: Any?): JSValue
    
    /**
     * Get a global variable from the JavaScript context.
     * 
     * @param name Name of the global variable
     * @return The value, or null if not found
     */
    fun getGlobal(name: String): JSValue?
    
    /**
     * Set a global variable in the JavaScript context.
     * 
     * @param name Name of the global variable
     * @param value Value to set
     */
    fun setGlobal(name: String, value: Any?)
    
    /**
     * Register a native function that can be called from JavaScript.
     * 
     * @param name Name of the function in JavaScript
     * @param function The native function implementation
     */
    fun registerFunction(name: String, function: JSNativeFunction)
    
    /**
     * Dispose of the engine instance and release resources.
     * The instance cannot be used after this call.
     */
    fun dispose()
    
    /**
     * Check if the engine instance is still valid.
     * 
     * @return true if the instance can be used
     */
    fun isValid(): Boolean
}

/**
 * Represents a JavaScript value.
 */
interface JSValue {
    /**
     * Check if this value is null or undefined.
     */
    fun isNullOrUndefined(): Boolean
    
    /**
     * Check if this value is a string.
     */
    fun isString(): Boolean
    
    /**
     * Check if this value is a number.
     */
    fun isNumber(): Boolean
    
    /**
     * Check if this value is a boolean.
     */
    fun isBoolean(): Boolean
    
    /**
     * Check if this value is an object.
     */
    fun isObject(): Boolean
    
    /**
     * Check if this value is an array.
     */
    fun isArray(): Boolean
    
    /**
     * Check if this value is a function.
     */
    fun isFunction(): Boolean
    
    /**
     * Get this value as a string.
     * 
     * @return The string value, or null if not a string
     */
    fun asString(): String?
    
    /**
     * Get this value as an integer.
     * 
     * @return The integer value, or null if not a number
     */
    fun asInt(): Int?
    
    /**
     * Get this value as a long.
     * 
     * @return The long value, or null if not a number
     */
    fun asLong(): Long?
    
    /**
     * Get this value as a double.
     * 
     * @return The double value, or null if not a number
     */
    fun asDouble(): Double?
    
    /**
     * Get this value as a boolean.
     * 
     * @return The boolean value, or null if not a boolean
     */
    fun asBoolean(): Boolean?
    
    /**
     * Get a property from this object.
     * 
     * @param key Property name
     * @return The property value, or null if not found
     */
    fun getProperty(key: String): JSValue?
    
    /**
     * Set a property on this object.
     * 
     * @param key Property name
     * @param value Property value
     */
    fun setProperty(key: String, value: Any?)
    
    /**
     * Get the keys of this object.
     * 
     * @return List of property names
     */
    fun getKeys(): List<String>
    
    /**
     * Get an element from this array.
     * 
     * @param index Array index
     * @return The element value, or null if out of bounds
     */
    fun getArrayElement(index: Int): JSValue?
    
    /**
     * Get the length of this array.
     * 
     * @return Array length, or 0 if not an array
     */
    fun getArrayLength(): Int
    
    /**
     * Convert this value to a JSON string.
     * 
     * @return JSON representation
     */
    fun toJson(): String
    
    /**
     * Get the underlying native value (platform-specific).
     * 
     * @return The native value
     */
    fun getNativeValue(): Any?
}

/**
 * Native function that can be called from JavaScript.
 */
fun interface JSNativeFunction {
    /**
     * Execute the native function.
     * 
     * @param args Arguments passed from JavaScript
     * @return Result to return to JavaScript
     */
    suspend fun invoke(args: List<JSValue>): Any?
}

/**
 * Capabilities of a JavaScript engine.
 */
@Serializable
data class JSEngineCapabilities(
    /** Engine name (e.g., "GraalVM", "J2V8", "QuickJS") */
    val engineName: String,
    /** Engine version */
    val engineVersion: String,
    /** Supported ECMAScript version (e.g., "ES2022") */
    val ecmaScriptVersion: String,
    /** Whether the engine supports ES modules */
    val supportsModules: Boolean = false,
    /** Whether the engine supports async/await */
    val supportsAsync: Boolean = true,
    /** Whether the engine supports Promises */
    val supportsPromises: Boolean = true,
    /** Whether the engine supports WebAssembly */
    val supportsWasm: Boolean = false,
    /** Maximum memory limit in bytes (0 = unlimited) */
    val maxMemoryBytes: Long = 0,
    /** Whether the engine supports debugging */
    val supportsDebugging: Boolean = false
)
