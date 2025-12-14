package ireader.domain.usersource.repository

import ireader.core.prefs.PreferenceStore
import ireader.domain.usersource.model.UserSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Implementation of UserSourceRepository using PreferenceStore for persistence.
 */
class UserSourceRepositoryImpl(
    private val preferenceStore: PreferenceStore
) : UserSourceRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }
    
    private val sourcesPreference = preferenceStore.getString(
        key = PREF_KEY_USER_SOURCES,
        defaultValue = "[]"
    )
    
    private val _sources = MutableStateFlow<List<UserSource>>(emptyList())
    
    init {
        loadSources()
    }
    
    private fun loadSources() {
        try {
            val jsonString = sourcesPreference.get()
            if (jsonString.isNotBlank() && jsonString != "[]") {
                val loaded = json.decodeFromString(
                    ListSerializer(UserSource.serializer()),
                    jsonString
                )
                _sources.value = loaded
            }
        } catch (e: Exception) {
            _sources.value = emptyList()
        }
    }
    
    private fun saveSources() {
        try {
            val jsonString = json.encodeToString(
                ListSerializer(UserSource.serializer()),
                _sources.value
            )
            sourcesPreference.set(jsonString)
        } catch (e: Exception) {
            // Log error
        }
    }
    
    override fun getAllAsFlow(): Flow<List<UserSource>> = _sources.asStateFlow()
    
    override suspend fun getAll(): List<UserSource> = _sources.value
    
    override suspend fun getEnabled(): List<UserSource> = _sources.value.filter { it.enabled }
    
    override suspend fun getByUrl(sourceUrl: String): UserSource? {
        return _sources.value.find { it.sourceUrl == sourceUrl }
    }
    
    override suspend fun getById(sourceId: Long): UserSource? {
        return _sources.value.find { it.generateId() == sourceId }
    }
    
    override suspend fun upsert(source: UserSource) {
        val current = _sources.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.sourceUrl == source.sourceUrl }
        
        if (existingIndex >= 0) {
            current[existingIndex] = source
        } else {
            current.add(source)
        }
        
        _sources.value = current
        saveSources()
    }
    
    override suspend fun upsertAll(sources: List<UserSource>) {
        val current = _sources.value.toMutableList()
        
        sources.forEach { source ->
            val existingIndex = current.indexOfFirst { it.sourceUrl == source.sourceUrl }
            if (existingIndex >= 0) {
                current[existingIndex] = source
            } else {
                current.add(source)
            }
        }
        
        _sources.value = current
        saveSources()
    }
    
    override suspend fun delete(sourceUrl: String) {
        val current = _sources.value.toMutableList()
        current.removeAll { it.sourceUrl == sourceUrl }
        _sources.value = current
        saveSources()
    }
    
    override suspend fun deleteById(sourceId: Long) {
        val current = _sources.value.toMutableList()
        current.removeAll { it.generateId() == sourceId }
        _sources.value = current
        saveSources()
    }
    
    override suspend fun deleteAll() {
        _sources.value = emptyList()
        saveSources()
    }
    
    override suspend fun setEnabled(sourceUrl: String, enabled: Boolean) {
        val source = getByUrl(sourceUrl) ?: return
        upsert(source.copy(enabled = enabled))
    }
    
    override suspend fun updateOrder(sourceUrl: String, newOrder: Int) {
        val source = getByUrl(sourceUrl) ?: return
        upsert(source.copy(customOrder = newOrder))
    }
    
    override suspend fun getByGroup(group: String): List<UserSource> {
        return _sources.value.filter { it.sourceGroup == group }
    }
    
    override suspend fun getGroups(): List<String> {
        return _sources.value
            .map { it.sourceGroup }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
    
    override suspend fun exportToJson(): String {
        return json.encodeToString(
            ListSerializer(UserSource.serializer()),
            _sources.value
        )
    }
    
    override suspend fun importFromJson(jsonString: String): Result<Int> {
        return try {
            val trimmed = jsonString.trim()
            val imported = if (trimmed.startsWith("[")) {
                json.decodeFromString(ListSerializer(UserSource.serializer()), trimmed)
            } else {
                listOf(json.decodeFromString(UserSource.serializer(), trimmed))
            }
            
            upsertAll(imported)
            Result.success(imported.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        private const val PREF_KEY_USER_SOURCES = "user_sources_json"
    }
}
