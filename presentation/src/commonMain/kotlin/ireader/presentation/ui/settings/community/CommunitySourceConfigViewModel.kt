package ireader.presentation.ui.settings.community

import ireader.domain.community.CommunityPreferences
import ireader.domain.community.CommunityRepository
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.launch

data class CommunitySourceConfigState(
    val communitySourceEnabled: Boolean = true,
    val communitySourceUrl: String = "",
    val communitySourceApiKey: String = "",
    val contributorName: String = "",
    val autoShareTranslations: Boolean = false,
    val showContributorBadge: Boolean = true,
    val preferredLanguage: String = "en",
    val showNsfwContent: Boolean = false,
    val minimumRating: Int = 0,
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val error: String? = null
)

class CommunitySourceConfigViewModel(
    private val communityPreferences: CommunityPreferences,
    private val communityRepository: CommunityRepository? = null
) : StateViewModel<CommunitySourceConfigState>(CommunitySourceConfigState()) {
    
    init {
        loadConfiguration()
    }
    
    private fun loadConfiguration() {
        scope.launch {
            updateState { it.copy(
                communitySourceEnabled = communityPreferences.communitySourceEnabled().get(),
                communitySourceUrl = communityPreferences.communitySourceUrl().get(),
                communitySourceApiKey = communityPreferences.communitySourceApiKey().get(),
                contributorName = communityPreferences.contributorName().get(),
                autoShareTranslations = communityPreferences.autoShareTranslations().get(),
                showContributorBadge = communityPreferences.showContributorBadge().get(),
                preferredLanguage = communityPreferences.preferredLanguage().get(),
                showNsfwContent = communityPreferences.showNsfwContent().get(),
                minimumRating = communityPreferences.minimumRating().get()
            )}
        }
    }
    
    fun setCommunitySourceEnabled(enabled: Boolean) {
        updateState { it.copy(communitySourceEnabled = enabled) }
        scope.launch {
            communityPreferences.communitySourceEnabled().set(enabled)
        }
    }
    
    fun setCommunitySourceUrl(url: String) {
        updateState { it.copy(communitySourceUrl = url) }
    }
    
    fun setCommunitySourceApiKey(apiKey: String) {
        updateState { it.copy(communitySourceApiKey = apiKey) }
    }
    
    fun setContributorName(name: String) {
        updateState { it.copy(contributorName = name) }
        scope.launch {
            communityPreferences.contributorName().set(name)
        }
    }
    
    fun setAutoShareTranslations(enabled: Boolean) {
        updateState { it.copy(autoShareTranslations = enabled) }
        scope.launch {
            communityPreferences.autoShareTranslations().set(enabled)
        }
    }
    
    fun setShowContributorBadge(show: Boolean) {
        updateState { it.copy(showContributorBadge = show) }
        scope.launch {
            communityPreferences.showContributorBadge().set(show)
        }
    }
    
    fun setPreferredLanguage(language: String) {
        updateState { it.copy(preferredLanguage = language) }
        scope.launch {
            communityPreferences.preferredLanguage().set(language)
        }
    }
    
    fun setShowNsfwContent(show: Boolean) {
        updateState { it.copy(showNsfwContent = show) }
        scope.launch {
            communityPreferences.showNsfwContent().set(show)
        }
    }
    
    fun setMinimumRating(rating: Int) {
        updateState { it.copy(minimumRating = rating) }
        scope.launch {
            communityPreferences.minimumRating().set(rating)
        }
    }
    
    fun saveConfiguration() {
        scope.launch {
            try {
                communityPreferences.communitySourceUrl().set(currentState.communitySourceUrl)
                communityPreferences.communitySourceApiKey().set(currentState.communitySourceApiKey)
                
                updateState { it.copy(
                    testResult = "✓ Configuration saved successfully!",
                    error = null
                )}
            } catch (e: Exception) {
                updateState { it.copy(
                    error = "Failed to save configuration: ${e.message}"
                )}
            }
        }
    }
    
    fun testConnection() {
        scope.launch {
            updateState { it.copy(isTesting = true, testResult = null) }
            
            try {
                // Test by fetching latest books
                val result = communityRepository?.getLatestBooks(1)
                
                if (result != null && result.mangas.isNotEmpty()) {
                    updateState { it.copy(
                        isTesting = false,
                        testResult = "✓ Connection successful! Found ${result.mangas.size} books."
                    )}
                } else if (result != null) {
                    updateState { it.copy(
                        isTesting = false,
                        testResult = "✓ Connection successful! No books found yet."
                    )}
                } else {
                    updateState { it.copy(
                        isTesting = false,
                        testResult = "✗ Connection failed: Repository not available"
                    )}
                }
            } catch (e: Exception) {
                updateState { it.copy(
                    isTesting = false,
                    testResult = "✗ Connection failed: ${e.message}"
                )}
            }
        }
    }
    
    fun clearError() {
        updateState { it.copy(error = null) }
    }
}
