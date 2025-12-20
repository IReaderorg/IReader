package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.characterart.CharacterArtDetailScreen
import ireader.presentation.ui.characterart.CharacterArtViewModel
import kotlinx.coroutines.launch

/**
 * Screen specification for Character Art Detail
 */
class CharacterArtDetailScreenSpec(
    private val artId: String
) {
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: CharacterArtViewModel = getIViewModel()
        val state by vm.state.collectAsState()
        val clipboardManager = LocalClipboardManager.current
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        
        // Load the art by ID when screen is shown
        LaunchedEffect(artId) {
            vm.loadArtById(artId)
        }
        
        val selectedArt = state.selectedArt
        
        if (selectedArt != null && selectedArt.id == artId) {
            CharacterArtDetailScreen(
                art = selectedArt,
                onBack = { navController.safePopBackStack() },
                onLikeClick = { vm.toggleLike(artId) },
                onShareClick = {
                    // Create share text with art info
                    val shareText = buildString {
                        append("Check out this character art!\n\n")
                        append("Character: ${selectedArt.characterName}\n")
                        append("From: ${selectedArt.bookTitle}\n")
                        if (selectedArt.aiModel.isNotBlank()) {
                            append("AI Model: ${selectedArt.aiModel}\n")
                        }
                        if (selectedArt.imageUrl.isNotBlank()) {
                            append("\nImage: ${selectedArt.imageUrl}")
                        }
                    }
                    // Copy to clipboard as a simple share mechanism
                    clipboardManager.setText(AnnotatedString(shareText))
                    scope.launch {
                        snackbarHostState.showSnackbar("Art info copied to clipboard!")
                    }
                },
                onReportClick = { vm.reportArt(artId, "Reported from detail screen") },
                onDeleteClick = { 
                    vm.deleteArt(artId)
                    navController.safePopBackStack()
                },
                isOwner = state.isAdmin // Allow admins to delete any art
            )
            
            // Snackbar for share feedback
            SnackbarHost(hostState = snackbarHostState)
        } else {
            // Loading state
            Scaffold { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
