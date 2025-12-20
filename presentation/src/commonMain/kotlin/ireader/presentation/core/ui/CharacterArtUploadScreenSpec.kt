package ireader.presentation.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import ireader.data.characterart.ImageProvider
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.characterart.AIProviderOption
import ireader.presentation.ui.characterart.CharacterArtViewModel
import ireader.presentation.ui.characterart.GeminiModelInfo
import ireader.presentation.ui.characterart.UploadCharacterArtScreen
import ireader.presentation.ui.characterart.rememberImagePicker
import kotlinx.coroutines.launch
import ireader.presentation.core.safePopBackStack
/**
 * Screen specification for Character Art Upload
 * @param prefilledBookTitle Optional book title from chapter art generation
 * @param prefilledChapterTitle Optional chapter title from chapter art generation
 * @param prefilledPrompt Optional AI prompt from chapter art generation
 */
class CharacterArtUploadScreenSpec(
    private val prefilledBookTitle: String = "",
    private val prefilledChapterTitle: String = "",
    private val prefilledPrompt: String = ""
) {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: CharacterArtViewModel = getIViewModel()
        val state by vm.state.collectAsState()
        val scope = rememberCoroutineScope()
        
        var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
        var selectedImagePath by remember { mutableStateOf<String?>(null) }
        
        // Track if we've applied prefilled values
        var hasAppliedPrefill by remember { mutableStateOf(false) }
        
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
        
        // Convert ImageProvider to AIProviderOption for UI
        val selectedProvider = remember(state.selectedProvider) {
            when (state.selectedProvider) {
                ImageProvider.GEMINI -> AIProviderOption.GEMINI
                ImageProvider.HUGGING_FACE -> AIProviderOption.HUGGING_FACE
                ImageProvider.POLLINATIONS -> AIProviderOption.POLLINATIONS
                ImageProvider.STABILITY_AI -> AIProviderOption.STABILITY_AI
            }
        }
        
        UploadCharacterArtScreen(
            onBack = {
                navController.safePopBackStack()
            },
            onPickImage = {
                // Launch the picker and handle the result
                scope.launch {
                    imagePicker.pickImage(
                        onImagePicked = { bytes, _ ->
                            selectedImageBytes = bytes
                            selectedImagePath = imagePicker.getSelectedImagePath()
                        },
                        onError = { /* Handle error */ }
                    )
                }
            },
            onGenerateImage = { provider, apiKey, prompt, characterName, bookTitle, style, modelId ->
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
                    navController.safePopBackStack()
                }
            },
            selectedImagePath = selectedImagePath,
            generatedImagePreview = state.generatedImageBytes,
            isUploading = state.isUploading,
            isGenerating = state.isGenerating,
            uploadProgress = state.uploadProgress,
            generationError = state.generationError,
            // Provider selection
            selectedProvider = selectedProvider,
            onProviderSelect = { uiProvider ->
                val domainProvider = when (uiProvider) {
                    AIProviderOption.GEMINI -> ImageProvider.GEMINI
                    AIProviderOption.HUGGING_FACE -> ImageProvider.HUGGING_FACE
                    AIProviderOption.POLLINATIONS -> ImageProvider.POLLINATIONS
                    AIProviderOption.STABILITY_AI -> ImageProvider.STABILITY_AI
                }
                vm.setProvider(domainProvider)
            },
            // Model selection
            availableModels = availableModels,
            selectedModel = selectedModel,
            isLoadingModels = state.isLoadingModels,
            onModelSelect = { modelInfo ->
                // Find the matching ImageModel and select it
                state.availableModels.find { it.id == modelInfo.id }?.let { model ->
                    vm.selectModel(model)
                }
            },
            onFetchModels = { provider, apiKey ->
                val domainProvider = when (provider) {
                    AIProviderOption.GEMINI -> ImageProvider.GEMINI
                    AIProviderOption.HUGGING_FACE -> ImageProvider.HUGGING_FACE
                    AIProviderOption.POLLINATIONS -> ImageProvider.POLLINATIONS
                    AIProviderOption.STABILITY_AI -> ImageProvider.STABILITY_AI
                }
                vm.setProvider(domainProvider)
            },
            // API keys
            onGeminiApiKeyChanged = { apiKey ->
                vm.setGeminiApiKey(apiKey)
            },
            onHuggingFaceApiKeyChanged = { apiKey ->
                vm.setHuggingFaceApiKey(apiKey)
            },
            onStabilityAiApiKeyChanged = { apiKey ->
                vm.setStabilityAiApiKey(apiKey)
            },
            initialGeminiApiKey = state.geminiApiKey,
            initialHuggingFaceApiKey = state.huggingFaceApiKey,
            initialStabilityAiApiKey = state.stabilityAiApiKey,
            // Prefilled values from chapter art generation
            prefilledBookTitle = prefilledBookTitle,
            prefilledChapterTitle = prefilledChapterTitle,
            prefilledPrompt = prefilledPrompt,
            paddingValues = PaddingValues(0.dp)
        )
    }
}
