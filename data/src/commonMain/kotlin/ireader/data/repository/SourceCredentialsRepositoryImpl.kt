package ireader.data.repository

import ireader.core.prefs.PreferenceStore
import ireader.domain.data.repository.SourceCredentialsRepository

/**
 * Implementation of SourceCredentialsRepository using PreferenceStore
 * Note: In production, credentials should be stored using platform-specific secure storage
 * (e.g., Android Keystore, iOS Keychain)
 */
class SourceCredentialsRepositoryImpl(
    private val preferenceStore: PreferenceStore
) : SourceCredentialsRepository {
    
    private fun getUsernameKey(sourceId: Long) = "source_${sourceId}_username"
    private fun getPasswordKey(sourceId: Long) = "source_${sourceId}_password"
    
    override suspend fun storeCredentials(sourceId: Long, username: String, password: String) {
        preferenceStore.getString(getUsernameKey(sourceId)).set(username)
        preferenceStore.getString(getPasswordKey(sourceId)).set(password)
    }
    
    override suspend fun getCredentials(sourceId: Long): Pair<String, String>? {
        val username = preferenceStore.getString(getUsernameKey(sourceId)).get()
        val password = preferenceStore.getString(getPasswordKey(sourceId)).get()
        
        return if (username.isNotBlank() && password.isNotBlank()) {
            Pair(username, password)
        } else {
            null
        }
    }
    
    override suspend fun removeCredentials(sourceId: Long) {
        preferenceStore.getString(getUsernameKey(sourceId)).delete()
        preferenceStore.getString(getPasswordKey(sourceId)).delete()
    }
    
    override suspend fun hasCredentials(sourceId: Long): Boolean {
        val username = preferenceStore.getString(getUsernameKey(sourceId)).get()
        val password = preferenceStore.getString(getPasswordKey(sourceId)).get()
        return username.isNotBlank() && password.isNotBlank()
    }
}
