package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.characterart.CharacterArtDetailScreen
import ireader.presentation.ui.characterart.CharacterArtViewModel

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
        
        // Load the art by ID when screen is shown
        LaunchedEffect(artId) {
            vm.loadArtById(artId)
        }
        
        val selectedArt = state.selectedArt
        
        if (selectedArt != null && selectedArt.id == artId) {
            CharacterArtDetailScreen(
                art = selectedArt,
                onBack = { navController.popBackStack() },
                onLikeClick = { vm.toggleLike(artId) },
                onShareClick = { /* TODO: Implement share */ },
                onReportClick = { vm.reportArt(artId, "Reported from detail screen") },
                onDeleteClick = if (selectedArt.submitterId == state.selectedArt?.submitterId) {
                    { vm.deleteArt(artId) }
                } else null,
                isOwner = false // TODO: Check if current user is owner
            )
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
