package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.characterart.CharacterArtGalleryScreen
import ireader.presentation.ui.characterart.CharacterArtViewModel

/**
 * Screen specification for Character Art Gallery
 */
class CharacterArtGalleryScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: CharacterArtViewModel = getIViewModel()
        Scaffold { paddingValues ->
            CharacterArtGalleryScreen(
                vm = vm,
                onBack = {
                    navController.safePopBackStack()
                },
                onArtClick = { art ->
                    // Navigate to detail screen with art ID
                    navController.navigate("${NavigationRoutes.characterArtDetail}/${art.id}")
                },
                onUploadClick = {
                    navController.navigate(NavigationRoutes.characterArtUpload)
                },
                paddingValues = paddingValues
            )
        }

    }
}
