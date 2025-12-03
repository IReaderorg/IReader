package ireader.presentation.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.characterart.CharacterArtViewModel
import ireader.presentation.ui.characterart.UploadCharacterArtScreen
import ireader.presentation.ui.characterart.rememberImagePicker
import kotlinx.coroutines.launch

/**
 * Screen specification for Character Art Upload
 */
class CharacterArtUploadScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: CharacterArtViewModel = getIViewModel()
        val state by vm.state.collectAsState()
        val scope = rememberCoroutineScope()
        
        var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
        var selectedImagePath by remember { mutableStateOf<String?>(null) }
        var generatedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
        var generationError by remember { mutableStateOf<String?>(null) }
        var isGenerating by remember { mutableStateOf(false) }
        
        val imagePicker = rememberImagePicker()
        
        UploadCharacterArtScreen(
            onBack = {
                navController.popBackStack()
            },
            onPickImage = {
                scope.launch {
                    imagePicker.pickImage(
                        onImagePicked = { bytes, name ->
                            selectedImageBytes = bytes
                            selectedImagePath = imagePicker.getSelectedImagePath()
                        },
                        onError = { error ->
                            // Handle error - could show snackbar
                        }
                    )
                }
            },
            onGenerateImage = { apiKey, prompt, characterName, bookTitle, style ->
                // TODO: Implement Gemini generation
                isGenerating = true
                generationError = null
                scope.launch {
                    // Call GeminiImageGenerator here
                    // For now, just simulate
                    kotlinx.coroutines.delay(2000)
                    isGenerating = false
                    generationError = "Gemini integration coming soon"
                }
            },
            onSubmit = { characterName, bookTitle, bookAuthor, description, aiModel, prompt, tags ->
                val imageBytes = generatedImageBytes ?: selectedImageBytes
                if (imageBytes != null) {
                    vm.submitArt(
                        request = ireader.domain.models.characterart.SubmitCharacterArtRequest(
                            characterName = characterName,
                            bookTitle = bookTitle,
                            bookAuthor = bookAuthor,
                            description = description,
                            aiModel = aiModel,
                            prompt = prompt,
                            tags = tags
                        ),
                        imageBytes = imageBytes
                    )
                    navController.popBackStack()
                }
            },
            selectedImagePath = selectedImagePath,
            generatedImagePreview = generatedImageBytes,
            isUploading = state.isUploading,
            isGenerating = isGenerating,
            uploadProgress = state.uploadProgress,
            generationError = generationError,
            paddingValues = PaddingValues(0.dp)
        )
    }
}
