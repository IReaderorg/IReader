package ireader.presentation.ui.plugins.integration

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Plugin data storage API allowing plugins to save preferences and data
 * Requirements: 6.5
 */
interface PluginDataStorage {
    /**
     * Get a data store for a specific plugin
     * 
     * @param pluginId Plugin identifier
     * @return Plugin-specific data store
     */
    fun getDataStore(pluginId: String): PluginDataStore
}

/**
 * Data store for a specific plugin
 * Requirements: 6.5
 */
interface PluginDataStore {
    /**
     * Save a string value
     */
    suspend fun putString(key: String, value: String)
    
    /**
     * Get a string value
     */
    suspend fun getString(key: String, defaultValue: String = ""): String
    
    /**
     * Save an integer value
     */
    suspend fun putInt(key: String, value: Int)
    
    /**
     * Get an integer value
     */
    suspend fun getInt(key: String, defaultValue: Int = 0): Int
    
    /**
     * Save a boolean value
     */
    suspend fun putBoolean(key: String, value: Boolean)
    
    /**
     * Get a boolean value
     */
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * Save a long value
     */
    suspend fun putLong(key: String, value: Long)
    
    /**
     * Get a long value
     */
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long
    
    /**
     * Save a float value
     */
    suspend fun putFloat(key: String, value: Float)
    
    /**
     * Get a float value
     */
    suspend fun getFloat(key: String, defaultValue: Float = 0f): Float
    
    /**
     * Remove a value
     */
    suspend fun remove(key: String)
    
    /**
     * Clear all data for this plugin
     */
    suspend fun clear()
    
    /**
     * Observe a string value
     */
    fun observeString(key: String, defaultValue: String = ""): Flow<String>
    
    /**
     * Observe an integer value
     */
    fun observeInt(key: String, defaultValue: Int = 0): Flow<Int>
    
    /**
     * Observe a boolean value
     */
    fun observeBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean>
}

/**
 * In-memory implementation of PluginDataStorage
 * In production, this would be backed by persistent storage (database or preferences)
 */
class InMemoryPluginDataStorage : PluginDataStorage {
    private val stores = mutableMapOf<String, PluginDataStore>()
    private val mutex = Mutex()
    
    override fun getDataStore(pluginId: String): PluginDataStore {
        return stores.getOrPut(pluginId) {
            InMemoryPluginDataStore()
        }
    }
}

/**
 * In-memory implementation of PluginDataStore
 * In production, this would be backed by persistent storage
 */
private class InMemoryPluginDataStore : PluginDataStore {
    private val data = mutableMapOf<String, Any>()
    private val mutex = Mutex()
    private val flows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    override suspend fun putString(key: String, value: String) {
        mutex.withLock {
            data[key] = value
            flows.getOrPut(key) { MutableStateFlow(null) }.value = value
        }
    }
    
    override suspend fun getString(key: String, defaultValue: String): String {
        return mutex.withLock {
            data[key] as? String ?: defaultValue
        }
    }
    
    override suspend fun putInt(key: String, value: Int) {
        mutex.withLock {
            data[key] = value
            flows.getOrPut(key) { MutableStateFlow(null) }.value = value
        }
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return mutex.withLock {
            data[key] as? Int ?: defaultValue
        }
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        mutex.withLock {
            data[key] = value
            flows.getOrPut(key) { MutableStateFlow(null) }.value = value
        }
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return mutex.withLock {
            data[key] as? Boolean ?: defaultValue
        }
    }
    
    override suspend fun putLong(key: String, value: Long) {
        mutex.withLock {
            data[key] = value
            flows.getOrPut(key) { MutableStateFlow(null) }.value = value
        }
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return mutex.withLock {
            data[key] as? Long ?: defaultValue
        }
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        mutex.withLock {
            data[key] = value
            flows.getOrPut(key) { MutableStateFlow(null) }.value = value
        }
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return mutex.withLock {
            data[key] as? Float ?: defaultValue
        }
    }
    
    override suspend fun remove(key: String) {
        mutex.withLock {
            data.remove(key)
            flows[key]?.value = null
        }
    }
    
    override suspend fun clear() {
        mutex.withLock {
            data.clear()
            flows.values.forEach { it.value = null }
        }
    }
    
    override fun observeString(key: String, defaultValue: String): Flow<String> {
        val flow = flows.getOrPut(key) { MutableStateFlow(data[key]) }
        return flow.map { it as? String ?: defaultValue }
    }
    
    override fun observeInt(key: String, defaultValue: Int): Flow<Int> {
        val flow = flows.getOrPut(key) { MutableStateFlow(data[key]) }
        return flow.map { it as? Int ?: defaultValue }
    }
    
    override fun observeBoolean(key: String, defaultValue: Boolean): Flow<Boolean> {
        val flow = flows.getOrPut(key) { MutableStateFlow(data[key]) }
        return flow.map { it as? Boolean ?: defaultValue }
    }
}
