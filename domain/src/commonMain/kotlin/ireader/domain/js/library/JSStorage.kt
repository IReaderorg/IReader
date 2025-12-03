package ireader.domain.js.library

import ireader.core.prefs.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * JavaScript Storage API implementation using PreferenceStore.
 * Provides persistent storage for JavaScript plugins with expiration support.
 */
open class JSStorage(
    private val preferenceStore: PreferenceStore,
    private val pluginId: String,
    private val validator: ireader.domain.js.util.JSPluginValidator = ireader.domain.js.util.JSPluginValidator()
) {
    
    @Serializable
    private data class StorageEntry(
        val value: String,
        val created: Long,
        val expires: Long? = null
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Stores a value with optional expiration.
     * @param key The storage key
     * @param value The value to store
     * @param expires Optional expiration timestamp in milliseconds
     */
    fun set(key: String, value: Any?, expires: Long? = null) {
        // Validate key for security
        val validationResult = validator.validateFileAccess(key, pluginId)
        if (!validationResult.isValid()) {
            throw IllegalStateException("Invalid storage key: ${validationResult.getError()}")
        }
        
        val prefKey = getPrefKey(key)
        val valueStr = when (value) {
            is String -> value
            else -> json.encodeToString(value)
        }
        
        val entry = StorageEntry(
            value = valueStr,
            created = currentTimeToLong(),
            expires = expires
        )
        
        val entryJson = json.encodeToString(entry)
        preferenceStore.getString(prefKey).set(entryJson)
    }
    
    /**
     * Retrieves a value from storage.
     * Returns null if the key doesn't exist or has expired.
     * @param key The storage key
     * @return The stored value, or null
     */
    fun get(key: String): Any? {
        // Validate key for security
        val validationResult = validator.validateFileAccess(key, pluginId)
        if (!validationResult.isValid()) {
            throw IllegalStateException("Invalid storage key: ${validationResult.getError()}")
        }
        
        val prefKey = getPrefKey(key)
        val entryJson = preferenceStore.getString(prefKey).get()
        
        if (entryJson.isEmpty()) {
            return null
        }
        
        return try {
            val entry = json.decodeFromString<StorageEntry>(entryJson)
            
            // Check expiration
            if (entry.expires != null) {
                val now = currentTimeToLong()
                if (now > entry.expires) {
                    delete(key)
                    return null
                }
            }
            
            entry.value
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Deletes a value from storage.
     * @param key The storage key
     */
    fun delete(key: String) {
        // Validate key for security
        val validationResult = validator.validateFileAccess(key, pluginId)
        if (!validationResult.isValid()) {
            throw IllegalStateException("Invalid storage key: ${validationResult.getError()}")
        }
        
        val prefKey = getPrefKey(key)
        preferenceStore.getString(prefKey).delete()
    }
    
    /**
     * Clears all storage for this plugin.
     */
    fun clearAll() {
        // Note: PreferenceStore doesn't provide a way to list all keys
        // This is a limitation that would need platform-specific implementation
        // For now, we'll document this limitation
    }
    
    /**
     * Gets all storage keys for this plugin.
     * @return List of storage keys
     */
    fun getAllKeys(): List<String> {
        // Note: PreferenceStore doesn't provide a way to list all keys
        // This is a limitation that would need platform-specific implementation
        return emptyList()
    }
    
    /**
     * Generates the preference key for a storage key.
     */
    private fun getPrefKey(key: String): String {
        return "js_plugin_${pluginId}_${key}"
    }
}

/**
 * In-memory storage implementation for LocalStorage compatibility.
 */
class JSLocalStorage : JSStorage(InMemoryPreferenceStore(), "local") {
    private val storage = mutableMapOf<String, Any?>()
    
    fun setLocal(key: String, value: Any?, expires: Long? = null) {
        storage[key] = value
    }
    
    fun getLocal(key: String): Any? {
        return storage[key]
    }
    
    fun deleteLocal(key: String) {
        storage.remove(key)
    }
    
    fun clearAllLocal() {
        storage.clear()
    }
    
    fun getAllKeysLocal(): List<String> {
        return storage.keys.toList()
    }
}

/**
 * In-memory storage implementation for SessionStorage compatibility.
 */
class JSSessionStorage : JSStorage(InMemoryPreferenceStore(), "session") {
    private val storage = mutableMapOf<String, Any?>()
    
    fun setSession(key: String, value: Any?, expires: Long? = null) {
        storage[key] = value
    }
    
    fun getSession(key: String): Any? {
        return storage[key]
    }
    
    fun deleteSession(key: String) {
        storage.remove(key)
    }
    
    fun clearAllSession() {
        storage.clear()
    }
    
    fun getAllKeysSession(): List<String> {
        return storage.keys.toList()
    }
}

/**
 * Dummy in-memory preference store for LocalStorage and SessionStorage.
 */
private class InMemoryPreferenceStore : PreferenceStore {
    override fun getString(key: String, defaultValue: String) = object : ireader.core.prefs.Preference<String> {
        private var value = defaultValue
        override fun key() = key
        override fun get() = value
        override fun set(value: String) { this.value = value }
        override fun isSet() = true
        override fun delete() { value = defaultValue }
        override fun defaultValue() = defaultValue
        override fun changes(): Flow<String> = throw UnsupportedOperationException()
        override fun stateIn(scope: CoroutineScope): StateFlow<String> = throw UnsupportedOperationException()
    }
    
    override fun getLong(key: String, defaultValue: Long) = throw UnsupportedOperationException()
    override fun getInt(key: String, defaultValue: Int) = throw UnsupportedOperationException()
    override fun getFloat(key: String, defaultValue: Float) = throw UnsupportedOperationException()
    override fun getBoolean(key: String, defaultValue: Boolean) = throw UnsupportedOperationException()
    override fun getStringSet(key: String, defaultValue: Set<String>) = throw UnsupportedOperationException()
    override fun <T> getObject(key: String, defaultValue: T, serializer: (T) -> String, deserializer: (String) -> T) = throw UnsupportedOperationException()
    override fun <T> getJsonObject(key: String, defaultValue: T, serializer: kotlinx.serialization.KSerializer<T>, serializersModule: kotlinx.serialization.modules.SerializersModule) = throw UnsupportedOperationException()
}
