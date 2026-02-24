package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for SyncPreferences.
 * Following TDD methodology: Write tests first, then implement.
 */
class SyncPreferencesTest {
    
    private class MockPreferenceStore : PreferenceStore {
        private val stringValues = mutableMapOf<String, String>()
        
        override fun getString(key: String, defaultValue: String): Preference<String> {
            return object : Preference<String> {
                override fun key(): String = key
                override fun get(): String = stringValues[key] ?: defaultValue
                override fun set(value: String) { stringValues[key] = value }
                override fun isSet(): Boolean = stringValues.containsKey(key)
                override fun delete() { stringValues.remove(key) }
                override fun defaultValue(): String = defaultValue
                override fun changes(): Flow<String> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<String> = MutableStateFlow(get())
            }
        }
        
        override fun getLong(key: String, defaultValue: Long): Preference<Long> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun getInt(key: String, defaultValue: Int): Preference<Int> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun <T> getObject(
            key: String,
            defaultValue: T,
            serializer: (T) -> String,
            deserializer: (String) -> T
        ): Preference<T> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun <T> getJsonObject(
            key: String,
            defaultValue: T,
            serializer: KSerializer<T>,
            serializersModule: SerializersModule
        ): Preference<T> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
    }
    
    private val preferenceStore = MockPreferenceStore()
    private val syncPreferences = SyncPreferences(preferenceStore)
    
    @Test
    fun `actAsServer should default to auto for automatic mode`() {
        // Arrange & Act
        val result = syncPreferences.actAsServer().get()
        
        // Assert
        assertEquals("auto", result, "Default should be 'auto' (automatic mode)")
    }
    
    @Test
    fun `getServerMode should return null for automatic mode by default`() {
        // Arrange & Act
        val result = syncPreferences.getServerMode()
        
        // Assert
        assertNull(result, "Default server mode should be null (automatic)")
    }
    
    @Test
    fun `actAsServer should store server when set to server mode`() {
        // Arrange
        val preference = syncPreferences.actAsServer()
        
        // Act
        preference.set("server")
        
        // Assert
        assertEquals("server", preference.get(), "Should store 'server' for server mode")
        assertEquals(true, syncPreferences.getServerMode(), "getServerMode should return true")
    }
    
    @Test
    fun `actAsServer should store client when set to client mode`() {
        // Arrange
        val preference = syncPreferences.actAsServer()
        
        // Act
        preference.set("client")
        
        // Assert
        assertEquals("client", preference.get(), "Should store 'client' for client mode")
        assertEquals(false, syncPreferences.getServerMode(), "getServerMode should return false")
    }
    
    @Test
    fun `actAsServer should allow switching back to automatic mode`() {
        // Arrange
        val preference = syncPreferences.actAsServer()
        preference.set("server")
        
        // Act
        preference.set("auto")
        
        // Assert
        assertEquals("auto", preference.get(), "Should allow switching back to 'auto'")
        assertNull(syncPreferences.getServerMode(), "getServerMode should return null for auto")
    }
}
