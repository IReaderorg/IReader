package ireader.presentation.ui.settings.community

import ireader.core.log.Log
import ireader.domain.community.CommunityPreferences
import ireader.domain.community.CommunityRepository
import ireader.domain.community.cloudflare.CommunityTranslationRepository
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.launch

data class CommunitySourceConfigState(
    val communitySourceEnabled: Boolean = true,
    val communitySourceUrl: String = "",
    val communitySourceApiKey: String = "",
    val contributorName: String = "",
    val autoShareTranslations: Boolean = false,
    val autoShareAiOnly: Boolean = true,
    val showContributorBadge: Boolean = true,
    val preferredLanguage: String = "en",
    val showNsfwContent: Boolean = false,
    val minimumRating: Int = 0,
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val error: String? = null,
    // Cloudflare D1 + R2 settings
    val cloudflareAccountId: String = "",
    val cloudflareApiToken: String = "",
    val cloudflareD1DatabaseId: String = "",
    val cloudflareR2BucketName: String = "",
    val cloudflareR2PublicUrl: String = "",
    val cloudflareCompressionEnabled: Boolean = true,
    val checkCommunityFirst: Boolean = true,
    val isCloudflareConfigured: Boolean = false,
    val isTestingCloudflare: Boolean = false,
    val cloudflareTestResult: String? = null
)

class CommunitySourceConfigViewModel(
    private val communityPreferences: CommunityPreferences,
    private val communityRepository: CommunityRepository? = null,
    private val translationRepository: CommunityTranslationRepository? = null
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
                autoShareAiOnly = communityPreferences.autoShareAiOnly().get(),
                showContributorBadge = communityPreferences.showContributorBadge().get(),
                preferredLanguage = communityPreferences.preferredLanguage().get(),
                showNsfwContent = communityPreferences.showNsfwContent().get(),
                minimumRating = communityPreferences.minimumRating().get(),
                // Cloudflare settings
                cloudflareAccountId = communityPreferences.cloudflareAccountId().get(),
                cloudflareApiToken = communityPreferences.cloudflareApiToken().get(),
                cloudflareD1DatabaseId = communityPreferences.cloudflareD1DatabaseId().get(),
                cloudflareR2BucketName = communityPreferences.cloudflareR2BucketName().get(),
                cloudflareR2PublicUrl = communityPreferences.cloudflareR2PublicUrl().get(),
                cloudflareCompressionEnabled = communityPreferences.cloudflareCompressionEnabled().get(),
                checkCommunityFirst = communityPreferences.checkCommunityFirst().get(),
                isCloudflareConfigured = communityPreferences.isCloudflareConfigured()
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
    
    fun setAutoShareAiOnly(enabled: Boolean) {
        updateState { it.copy(autoShareAiOnly = enabled) }
        scope.launch {
            communityPreferences.autoShareAiOnly().set(enabled)
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
    
    // ==================== Cloudflare Settings ====================
    
    fun setCloudflareAccountId(accountId: String) {
        updateState { it.copy(cloudflareAccountId = accountId) }
    }
    
    fun setCloudflareApiToken(token: String) {
        updateState { it.copy(cloudflareApiToken = token) }
    }
    
    fun setCloudflareD1DatabaseId(databaseId: String) {
        updateState { it.copy(cloudflareD1DatabaseId = databaseId) }
    }
    
    fun setCloudflareR2BucketName(bucketName: String) {
        updateState { it.copy(cloudflareR2BucketName = bucketName) }
    }
    
    fun setCloudflareR2PublicUrl(publicUrl: String) {
        updateState { it.copy(cloudflareR2PublicUrl = publicUrl) }
    }
    
    fun setCloudflareCompressionEnabled(enabled: Boolean) {
        updateState { it.copy(cloudflareCompressionEnabled = enabled) }
        scope.launch {
            communityPreferences.cloudflareCompressionEnabled().set(enabled)
        }
    }
    
    fun setCheckCommunityFirst(enabled: Boolean) {
        updateState { it.copy(checkCommunityFirst = enabled) }
        scope.launch {
            communityPreferences.checkCommunityFirst().set(enabled)
        }
    }
    
    fun saveCloudflareConfiguration() {
        scope.launch {
            try {
                communityPreferences.cloudflareAccountId().set(currentState.cloudflareAccountId)
                communityPreferences.cloudflareApiToken().set(currentState.cloudflareApiToken)
                communityPreferences.cloudflareD1DatabaseId().set(currentState.cloudflareD1DatabaseId)
                communityPreferences.cloudflareR2BucketName().set(currentState.cloudflareR2BucketName)
                communityPreferences.cloudflareR2PublicUrl().set(currentState.cloudflareR2PublicUrl)
                
                updateState { it.copy(
                    cloudflareTestResult = "✓ Cloudflare configuration saved!",
                    isCloudflareConfigured = communityPreferences.isCloudflareConfigured()
                )}
            } catch (e: Exception) {
                updateState { it.copy(
                    cloudflareTestResult = "✗ Failed to save: ${e.message}"
                )}
            }
        }
    }
    
    fun testCloudflareConnection() {
        scope.launch {
            updateState { it.copy(isTestingCloudflare = true, cloudflareTestResult = null) }
            
            // First validate the configuration is complete
            val isConfigured = currentState.cloudflareAccountId.isNotBlank() &&
                currentState.cloudflareApiToken.isNotBlank() &&
                currentState.cloudflareD1DatabaseId.isNotBlank() &&
                currentState.cloudflareR2BucketName.isNotBlank()
            
            if (!isConfigured) {
                updateState { it.copy(
                    isTestingCloudflare = false,
                    cloudflareTestResult = "✗ Please fill in all required fields"
                )}
                return@launch
            }
            
            // Try to initialize the D1 schema (this tests the connection)
            if (translationRepository != null) {
                try {
                    val result = translationRepository.initialize()
                    if (result.isSuccess) {
                        updateState { it.copy(
                            isTestingCloudflare = false,
                            cloudflareTestResult = "✓ Connection successful! D1 database ready."
                        )}
                    } else {
                        updateState { it.copy(
                            isTestingCloudflare = false,
                            cloudflareTestResult = "✗ D1 connection failed: ${result.exceptionOrNull()?.message}"
                        )}
                    }
                } catch (e: Exception) {
                    Log.error("Cloudflare test failed", e)
                    updateState { it.copy(
                        isTestingCloudflare = false,
                        cloudflareTestResult = "✗ Connection error: ${e.message}"
                    )}
                }
            } else {
                updateState { it.copy(
                    isTestingCloudflare = false,
                    cloudflareTestResult = "✓ Configuration looks valid! Save to apply and restart app to test."
                )}
            }
        }
    }
}
