package ireader.domain.usecases.reader

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.data.repository.TextReplacementRepository
import ireader.domain.models.entities.TextReplacement
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for TextReplacementUseCase focusing on:
 * - Issue #10: runBlocking in suspend context should be removed
 * - Issue #17: Regex detection logic duplication should be extracted
 */
class TextReplacementUseCaseTest {

    private class MockPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any>()

        override fun getString(key: String, defaultValue: String): Preference<String> =
            SimplePreference(key, defaultValue, values)

        override fun getLong(key: String, defaultValue: Long): Preference<Long> =
            SimplePreference(key, defaultValue, values)

        override fun getInt(key: String, defaultValue: Int): Preference<Int> =
            SimplePreference(key, defaultValue, values)

        override fun getFloat(key: String, defaultValue: Float): Preference<Float> =
            SimplePreference(key, defaultValue, values)

        override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> =
            SimplePreference(key, defaultValue, values)

        override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> =
            SimplePreference(key, defaultValue, values)

        override fun <T> getObject(
            key: String,
            defaultValue: T,
            serializer: (T) -> String,
            deserializer: (String) -> T
        ): Preference<T> =
            SimplePreference(key, defaultValue, values)

        override fun <T> getJsonObject(
            key: String,
            defaultValue: T,
            serializer: KSerializer<T>,
            serializersModule: SerializersModule
        ): Preference<T> =
            SimplePreference(key, defaultValue, values)
    }

    private class SimplePreference<T>(
        private val key: String,
        private val defaultValue: T,
        private val values: MutableMap<String, Any>
    ) : Preference<T> {
        @Suppress("UNCHECKED_CAST")
        override fun get(): T = values[key] as? T ?: defaultValue
        override fun set(value: T) { values[key] = value as Any }
        override fun isSet(): Boolean = values.containsKey(key)
        override fun delete() { values.remove(key) }
        override fun defaultValue(): T = defaultValue
        override fun key(): String = key
        override fun changes(): Flow<T> = MutableStateFlow(get())
        override fun stateIn(scope: CoroutineScope): StateFlow<T> = MutableStateFlow(get())
    }

    private class FakeTextReplacementRepository : TextReplacementRepository {
        var replacements: List<TextReplacement> = emptyList()
        var enabledGlobalReplacements: List<TextReplacement> = emptyList()
        var enabledBookReplacements: Map<Long, List<TextReplacement>> = emptyMap()

        override fun getGlobalReplacements(): Flow<List<TextReplacement>> = flowOf(replacements)

        override fun getReplacementsForBook(bookId: Long): Flow<List<TextReplacement>> =
            flowOf(replacements.filter { it.bookId == bookId })

        override suspend fun getEnabledGlobalReplacements(): List<TextReplacement> =
            enabledGlobalReplacements

        override suspend fun getEnabledReplacementsForBook(bookId: Long): List<TextReplacement> =
            enabledBookReplacements[bookId] ?: emptyList()

        override fun getBookSpecificReplacements(bookId: Long): Flow<List<TextReplacement>> =
            flowOf(replacements.filter { it.bookId == bookId })

        override suspend fun getReplacementById(id: Long): TextReplacement? =
            replacements.find { it.id == id }

        override suspend fun insert(replacement: TextReplacement): Long = 1L

        override suspend fun insertWithId(replacement: TextReplacement) {}

        override suspend fun update(replacement: TextReplacement) {}

        override suspend fun toggleEnabled(id: Long) {}

        override suspend fun delete(id: Long) {}

        override suspend fun deleteBookReplacements(bookId: Long) {}

        override suspend fun countReplacements(): Long = replacements.size.toLong()

        override suspend fun countEnabledReplacements(): Long =
            replacements.count { it.enabled }.toLong()
    }

    private val preferenceStore = MockPreferenceStore()
    private val readerPreferences = ReaderPreferences(preferenceStore)

    // Test for Issue #10: getEnabledReplacements should be suspend, not use runBlocking
    @Test
    fun `getEnabledReplacements should work in suspend context without blocking`() = runTest {
        // Arrange
        val repository = FakeTextReplacementRepository()
        repository.enabledGlobalReplacements = listOf(
            TextReplacement(
                id = 1,
                name = "Test",
                findText = "old",
                replaceText = "new",
                enabled = true,
                createdAt = 0L,
                updatedAt = 0L
            )
        )
        val useCase = TextReplacementUseCase(readerPreferences, repository)

        // Act - This should work in suspend context
        val result = useCase.applyReplacementsToText("old text", bookId = null)

        // Assert
        assertEquals("new text", result)
    }

    // Test for Issue #17: Regex detection logic should be extracted to helper
    @Test
    fun `isRegexPattern helper should detect regex metacharacters`() = runTest {
        // Arrange
        val repository = FakeTextReplacementRepository()
        val useCase = TextReplacementUseCase(readerPreferences, repository)

        // Test with regex pattern
        repository.enabledGlobalReplacements = listOf(
            TextReplacement(
                id = 1,
                name = "Regex test",
                findText = "test.*pattern",
                replaceText = "replaced",
                enabled = true,
                createdAt = 0L,
                updatedAt = 0L
            )
        )

        // Act
        val result = useCase.applyReplacementsToText("test some pattern here", bookId = null)

        // Assert
        assertTrue(result.contains("replaced"))
    }

    @Test
    fun `isRegexPattern helper should handle literal strings`() = runTest {
        // Arrange
        val repository = FakeTextReplacementRepository()
        repository.enabledGlobalReplacements = listOf(
            TextReplacement(
                id = 1,
                name = "Literal test",
                findText = "simple",
                replaceText = "replaced",
                enabled = true,
                createdAt = 0L,
                updatedAt = 0L
            )
        )
        val useCase = TextReplacementUseCase(readerPreferences, repository)

        // Act
        val result = useCase.applyReplacementsToText("simple text", bookId = null)

        // Assert
        assertEquals("replaced text", result)
    }

    @Test
    fun `testReplacement should use same regex detection logic`() {
        // Arrange
        val useCase = TextReplacementUseCase(readerPreferences, null)

        // Act - Test with regex
        val regexResult = useCase.testReplacement(
            text = "test123pattern",
            findText = "test\\d+pattern",
            replaceText = "replaced",
            caseSensitive = false
        )

        // Act - Test with literal
        val literalResult = useCase.testReplacement(
            text = "simple text",
            findText = "simple",
            replaceText = "replaced",
            caseSensitive = false
        )

        // Assert
        assertEquals("replaced", regexResult)
        assertEquals("replaced text", literalResult)
    }
}
