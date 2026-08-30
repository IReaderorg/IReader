package ireader.presentation.ui.settings.translation_suite

import androidx.compose.runtime.Stable
import ireader.domain.models.entities.TextReplacement
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.TranslationPreferences
import ireader.domain.usecases.reader.TextReplacementUseCase
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class TranslationEngineChoice {
    GOOGLE_TRANSLATE,
    DEEPL,
    LIBRE_TRANSLATE,
    CLOUDFLARE_AI,
    OPENAI
}

@Stable
data class GlossaryTermItem(
    val id: Long = 0L,
    val sourceTerm: String = "",
    val targetTerm: String = "",
    val notes: String = ""
)

@Stable
data class TextCleanupPreset(
    val id: String,
    val title: String,
    val description: String,
    val pattern: String,
    val isEnabled: Boolean = true
)

@Stable
data class TranslationSuiteState(
    val selectedEngine: TranslationEngineChoice = TranslationEngineChoice.GOOGLE_TRANSLATE,
    val targetLanguage: String = "English",
    val availableLanguages: List<String> = listOf("English", "Spanish", "French", "German", "Russian", "Japanese", "Chinese", "Korean", "Arabic", "Portuguese"),
    val apiKey: String = "",
    val autoTranslateChapters: Boolean = false,
    val autoTranslateNovelNames: Boolean = false,
    val glossaryTerms: List<GlossaryTermItem> = emptyList(),
    val glossarySearchQuery: String = "",
    val textReplacements: List<TextReplacement> = emptyList(),
    val activePresets: List<TextCleanupPreset> = listOf(
        TextCleanupPreset("nav_hints", "Strip Navigation Hints", "Removes 'Use arrow keys to navigate' hints", "Use arrow keys.*chapter", true),
        TextCleanupPreset("promo_ads", "Strip Promotional Ads", "Removes 'Visit site for more chapters' ads", "Visit.*for more chapters", true),
        TextCleanupPreset("watermarks", "Strip Source Watermarks", "Removes 'Read more at...' watermarks", "Read more at.*", true),
        TextCleanupPreset("brackets", "Remove Bracket Metadata", "Removes novel hosting bracket tags", "\\[(Updated|RAW|MTL)\\]", false)
    ),
    val showAddGlossaryDialog: Boolean = false,
    val showAddRuleDialog: Boolean = false
)

class TranslationSuiteViewModel(
    private val readerPreferences: ReaderPreferences,
    private val translationPreferences: TranslationPreferences,
    private val textReplacementUseCase: TextReplacementUseCase? = null
) : StateViewModel<TranslationSuiteState>(TranslationSuiteState()) {

    init {
        loadSettings()
        loadReplacements()
    }

    private fun loadSettings() {
        val autoNovel = translationPreferences.autoTranslateNovelNames().get()
        val apiKey = readerPreferences.openAIApiKey().get()

        updateState {
            it.copy(
                apiKey = apiKey,
                autoTranslateNovelNames = autoNovel
            )
        }
    }

    private fun loadReplacements() {
        scope.launch {
            val replacements = textReplacementUseCase?.getGlobalReplacements()
                ?.catch { }
                ?.firstOrNull() ?: emptyList()

            updateState { it.copy(textReplacements = replacements) }
        }
    }

    fun setEngine(engine: TranslationEngineChoice) {
        updateState { it.copy(selectedEngine = engine) }
    }

    fun setTargetLanguage(language: String) {
        updateState { it.copy(targetLanguage = language) }
    }

    fun setApiKey(key: String) {
        readerPreferences.openAIApiKey().set(key)
        updateState { it.copy(apiKey = key) }
    }

    fun toggleAutoTranslateChapters(enabled: Boolean) {
        updateState { it.copy(autoTranslateChapters = enabled) }
    }

    fun toggleAutoTranslateNovelNames(enabled: Boolean) {
        translationPreferences.autoTranslateNovelNames().set(enabled)
        updateState { it.copy(autoTranslateNovelNames = enabled) }
    }

    fun togglePreset(presetId: String, enabled: Boolean) {
        updateState {
            it.copy(
                activePresets = it.activePresets.map { preset ->
                    if (preset.id == presetId) preset.copy(isEnabled = enabled) else preset
                }
            )
        }
    }

    fun setGlossarySearch(query: String) {
        updateState { it.copy(glossarySearchQuery = query) }
    }

    fun addGlossaryTerm(source: String, target: String, notes: String = "") {
        if (source.isBlank() || target.isBlank()) return
        val newItem = GlossaryTermItem(
            id = ireader.core.util.currentTimeMillis(),
            sourceTerm = source.trim(),
            targetTerm = target.trim(),
            notes = notes.trim()
        )
        updateState {
            it.copy(
                glossaryTerms = it.glossaryTerms + newItem,
                showAddGlossaryDialog = false
            )
        }
    }

    fun deleteGlossaryTerm(id: Long) {
        updateState {
            it.copy(glossaryTerms = it.glossaryTerms.filterNot { term -> term.id == id })
        }
    }

    fun setShowAddGlossaryDialog(show: Boolean) {
        updateState { it.copy(showAddGlossaryDialog = show) }
    }

    fun setShowAddRuleDialog(show: Boolean) {
        updateState { it.copy(showAddRuleDialog = show) }
    }

    fun addCustomReplacement(findText: String, replaceText: String, isRegex: Boolean = false) {
        if (findText.isBlank()) return
        scope.launch {
            textReplacementUseCase?.addReplacement(
                name = findText,
                findText = findText,
                replaceText = replaceText
            )

            loadReplacements()
            updateState { it.copy(showAddRuleDialog = false) }
        }
    }

    fun deleteCustomReplacement(id: Long) {
        scope.launch {
            textReplacementUseCase?.deleteReplacement(id)
            loadReplacements()
        }
    }
}
