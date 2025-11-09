package ireader.presentation.ui.settings.donation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.domain.models.donation.DonationPromptMessage
import ireader.domain.models.donation.DonationTrigger
import ireader.domain.models.donation.toPromptMessage
import ireader.domain.usecases.donation.DonationUseCases
import kotlinx.coroutines.launch

/**
 * ViewModel for managing donation trigger state
 * This can be used as a shared state holder across the app
 * 
 * Example usage in a screen:
 * ```
 * @Composable
 * fun MyScreen() {
 *     val donationViewModel = getScreenModel<DonationTriggerViewModel>()
 *     
 *     // Show donation prompt dialog when triggered
 *     donationViewModel.currentPrompt?.let { promptMessage ->
 *         DonationPromptDialog(
 *             promptMessage = promptMessage,
 *             onDonateNow = {
 *                 donationViewModel.onDonateNow()
 *                 navigator.push(DonationScreen())
 *             },
 *             onMaybeLater = {
 *                 donationViewModel.onMaybeLater()
 *             }
 *         )
 *     }
 * }
 * ```
 */
class DonationTriggerViewModel(
    private val donationUseCases: DonationUseCases
) : StateScreenModel<DonationTriggerViewModel.State>(State()) {
    
    data class State(
        val currentPrompt: DonationPromptMessage? = null,
        val isLoading: Boolean = false
    )
    
    var currentPrompt by mutableStateOf<DonationPromptMessage?>(null)
        private set
    
    /**
     * Check if book completion should trigger a donation prompt
     */
    fun checkBookCompletion(chapterCount: Int, bookTitle: String) {
        screenModelScope.launch {
            val trigger = donationUseCases.donationTriggerManager.checkBookCompletion(
                chapterCount = chapterCount,
                bookTitle = bookTitle
            )
            
            if (trigger != null) {
                showPrompt(trigger)
            }
        }
    }
    
    /**
     * Check if source migration should trigger a donation prompt
     */
    fun checkSourceMigration(sourceName: String, chapterDifference: Int) {
        screenModelScope.launch {
            val trigger = donationUseCases.donationTriggerManager.checkSourceMigration(
                sourceName = sourceName,
                chapterDifference = chapterDifference
            )
            
            if (trigger != null) {
                showPrompt(trigger)
            }
        }
    }
    
    /**
     * Check if chapter milestone should trigger a donation prompt
     */
    fun checkChapterMilestone() {
        screenModelScope.launch {
            val trigger = donationUseCases.donationTriggerManager.checkChapterMilestone()
            
            if (trigger != null) {
                showPrompt(trigger)
            }
        }
    }
    
    /**
     * Show donation prompt with contextual message
     */
    private suspend fun showPrompt(trigger: DonationTrigger) {
        currentPrompt = trigger.toPromptMessage()
        donationUseCases.donationTriggerManager.recordPromptShown()
    }
    
    /**
     * Handle "Donate Now" button click
     * Dismisses the prompt and navigates to donation screen
     */
    fun onDonateNow() {
        currentPrompt = null
    }
    
    /**
     * Handle "Maybe Later" button click
     * Dismisses the prompt
     */
    fun onMaybeLater() {
        currentPrompt = null
    }
    
    /**
     * Get days remaining until next prompt can be shown
     */
    fun getDaysUntilNextPrompt(onResult: (Int) -> Unit) {
        screenModelScope.launch {
            val days = donationUseCases.donationTriggerManager.getDaysUntilNextPrompt()
            onResult(days)
        }
    }
}
