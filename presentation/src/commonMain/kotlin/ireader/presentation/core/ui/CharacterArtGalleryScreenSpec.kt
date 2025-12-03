package ireader.presentation.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
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
        
        CharacterArtGalleryScreen(
            vm = vm,
            onBack = {
                navController.popBackStack()
            },
            onArtClick = { art ->
                // Navigate to detail screen with art ID
                navController.navigate("${NavigationRoutes.characterArtDetail}/${art.id}")
            },
            onUploadClick = {
                navController.navigate(NavigationRoutes.characterArtUpload)
            },
            paddingValues = PaddingValues(0.dp)
        )
    }
}
