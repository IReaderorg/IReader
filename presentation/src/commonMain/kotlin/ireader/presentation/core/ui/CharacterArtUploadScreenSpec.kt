package ireader.presentation.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.characterart.CharacterArtViewModel
import ireader.presentation.ui.characterart.GeminiModelInfo
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
        
        val imagePicker = rememberImagePicker()
        
        // Convert ImageModel to GeminiModelInfo for UI
        val availableModels = remember(state.availableModels) {
            state.availableModels.map { model ->
                GeminiModelInfo(
                    id = model.id,
                    displayName = model.displayName,
                    description = model.description
                )
            }
        }
        
        val selectedModel = remember(state.selectedModel) {
            state.selectedModel?.let { model ->
                GeminiModelInfo(
                    id = model.id,
                    displayName = model.displayName,
                    description = model.description
                )
            }
        }
        
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
                vm.generateImage(prompt, characterName, bookTitle, style)
            },
            onSubmit = { characterName, bookTitle, bookAuthor, description, aiModel, prompt, tags ->
                val imageBytes = state.generatedImageBytes ?: selectedImageBytes
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
            generatedImagePreview = state.generatedImageBytes,
            isUploading = state.isUploading,
            isGenerating = state.isGenerating,
            uploadProgress = state.uploadProgress,
            generationError = state.generationError,
            availableModels = availableModels,
            selectedModel = selectedModel,
            isLoadingModels = state.isLoadingModels,
            onModelSelect = { modelInfo ->
                // Find the matching ImageModel and select it
                state.availableModels.find { it.id == modelInfo.id }?.let { model ->
                    vm.selectModel(model)
                }
            },
            onFetchModels = { apiKey ->
                vm.fetchAvailableModels(apiKey)
            },
            onApiKeyChanged = { apiKey ->
                vm.setGeminiApiKey(apiKey)
            },
            paddingValues = PaddingValues(0.dp)
        )
    }
}
