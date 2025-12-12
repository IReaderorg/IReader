package ireader.presentation.ui.characterart

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.characterart.ArtStyleFilter
import ireader.presentation.ui.component.isTableUi

/**
 * Image source mode for upload
 */
enum class ImageSourceMode {
    PICK_FILE,      // Pick from device
    GENERATE_AI     // Generate with Gemini
}

/**
 * Default Gemini models for image generation (only image generation models)
 */
private val defaultModels = listOf(
    GeminiModelInfo("imagen-4.0-generate-001", "Imagen 4", "Latest high quality image generation"),
    GeminiModelInfo("imagen-3.0-generate-002", "Imagen 3", "High quality image generation"),
    GeminiModelInfo("imagen-3.0-fast-generate-001", "Imagen 3 Fast", "Faster generation, slightly lower quality"),
    GeminiModelInfo("gemini-2.0-flash-preview-image-generation", "Gemini 2.0 Flash", "Multimodal image generation")
)

/**
 * Screen for uploading new character art with Gemini AI generation support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadCharacterArtScreen(
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    onGenerateImage: (apiKey: String, prompt: String, characterName: String, bookTitle: String, style: String) -> Unit,
    onSubmit: (
        characterName: String,
        bookTitle: String,
        bookAuthor: String,
        description: String,
        aiModel: String,
        prompt: String,
        tags: List<String>
    ) -> Unit,
    selectedImagePath: String?,
    generatedImagePreview: ByteArray?,
    isUploading: Boolean,
    isGenerating: Boolean,
    uploadProgress: Float,
    generationError: String?,
    // Model selection parameters
    availableModels: List<GeminiModelInfo> = defaultModels,
    selectedModel: GeminiModelInfo? = defaultModels.firstOrNull(),
    isLoadingModels: Boolean = false,
    onModelSelect: (GeminiModelInfo) -> Unit = {},
    onFetchModels: (String) -> Unit = {},
    onApiKeyChanged: (String) -> Unit = {},
    // Initial API key from preferences
    initialApiKey: String = "",
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {
    val isWideScreen = isTableUi()
    val scrollState = rememberScrollState()
    
    var characterName by remember { mutableStateOf("") }
    var bookTitle by remember { mutableStateOf("") }
    var bookAuthor by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var aiModel by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<List<ArtStyleFilter>>(emptyList()) }
    
    // Gemini AI generation state
    var imageSourceMode by remember { mutableStateOf(ImageSourceMode.PICK_FILE) }
    var geminiApiKey by remember(initialApiKey) { mutableStateOf(initialApiKey) }
    var generationPrompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("digital art") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    
    val hasImage = selectedImagePath != null || generatedImagePreview != null
    val isFormValid = characterName.isNotBlank() && 
                      bookTitle.isNotBlank() && 
                      hasImage
    
    // API Key dialog
    if (showApiKeyDialog) {
        GeminiApiKeyDialog(
            currentKey = geminiApiKey,
            onDismiss = { showApiKeyDialog = false },
            onSave = { key ->
                geminiApiKey = key
                onApiKeyChanged(key)
                // Fetch models when API key is set
                if (key.isNotBlank()) {
                    onFetchModels(key)
                }
                showApiKeyDialog = false
            }
        )
    }
    Scaffold { paddingValues ->
        Scaffold(
            modifier = modifier.padding(paddingValues),
            topBar = {
                UploadTopBar(onBack = onBack)
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = if (isWideScreen) 48.dp else 16.dp)
                        .padding(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    // Image source toggle
                    ImageSourceToggle(
                        selectedMode = imageSourceMode,
                        onModeChange = { imageSourceMode = it }
                    )

                    Spacer(Modifier.height(12.dp))

                    // Image section based on mode
                    when (imageSourceMode) {
                        ImageSourceMode.PICK_FILE -> {
                            ImagePickerSection(
                                selectedImagePath = selectedImagePath,
                                onPickImage = onPickImage,
                                isWideScreen = isWideScreen
                            )
                        }

                        ImageSourceMode.GENERATE_AI -> {
                            GeminiGeneratorSection(
                                apiKey = geminiApiKey,
                                prompt = generationPrompt,
                                characterName = characterName,
                                bookTitle = bookTitle,
                                selectedStyle = selectedStyle,
                                selectedModel = selectedModel,
                                availableModels = availableModels,
                                isLoadingModels = isLoadingModels,
                                generatedPreview = generatedImagePreview,
                                isGenerating = isGenerating,
                                error = generationError,
                                onApiKeyClick = { showApiKeyDialog = true },
                                onPromptChange = { generationPrompt = it },
                                onStyleChange = { selectedStyle = it },
                                onModelSelect = onModelSelect,
                                onFetchModels = { onFetchModels(geminiApiKey) },
                                onGenerate = {
                                    if (geminiApiKey.isNotBlank() && generationPrompt.isNotBlank()) {
                                        onGenerateImage(
                                            geminiApiKey,
                                            generationPrompt,
                                            characterName,
                                            bookTitle,
                                            selectedStyle
                                        )
                                        // Auto-fill AI model with selected model name
                                        aiModel = selectedModel?.displayName ?: "Gemini Imagen 3"
                                        prompt = generationPrompt
                                    }
                                },
                                isWideScreen = isWideScreen
                            )
                        }
                    }

                    // Character info section
                    SectionCard(title = "Character Info", icon = "👤") {
                        OutlinedTextField(
                            value = characterName,
                            onValueChange = { characterName = it },
                            label = { Text("Character Name *") },
                            placeholder = { Text("e.g., Harry Potter") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            placeholder = { Text("Brief description of the character...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Book info section
                    SectionCard(title = "Book Info", icon = "📚") {
                        OutlinedTextField(
                            value = bookTitle,
                            onValueChange = { bookTitle = it },
                            label = { Text("Book Title *") },
                            placeholder = { Text("e.g., Harry Potter and the Sorcerer's Stone") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = bookAuthor,
                            onValueChange = { bookAuthor = it },
                            label = { Text("Author") },
                            placeholder = { Text("e.g., J.K. Rowling") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // AI info section
                    SectionCard(title = "AI Generation Info", icon = "🤖") {
                        OutlinedTextField(
                            value = aiModel,
                            onValueChange = { aiModel = it },
                            label = { Text("AI Model Used") },
                            placeholder = { Text("e.g., Midjourney, DALL-E, Stable Diffusion") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            label = { Text("Prompt Used") },
                            placeholder = { Text("Share the prompt you used (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Style tags section
                    SectionCard(title = "Art Style", icon = "🎨") {
                        Text(
                            text = "Select applicable styles:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ArtStyleFilter.entries.filter { it != ArtStyleFilter.ALL }) { style ->
                                val isSelected = style in selectedTags
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedTags = if (isSelected) {
                                            selectedTags - style
                                        } else {
                                            selectedTags + style
                                        }
                                    },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(style.icon, fontSize = 14.sp)
                                            Text(style.displayName)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Guidelines
                    GuidelinesCard()
                }

                // Bottom submit button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (isUploading) {
                            LinearProgressIndicator(
                                progress = { uploadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Button(
                                onClick = {
                                    onSubmit(
                                        characterName,
                                        bookTitle,
                                        bookAuthor,
                                        description,
                                        aiModel,
                                        prompt,
                                        selectedTags.map { it.name }
                                    )
                                },
                                enabled = isFormValid,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Submit for Review",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadTopBar(onBack: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            
            Spacer(Modifier.width(8.dp))
            
            Text("✨", fontSize = 28.sp)
            
            Spacer(Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Upload Character Art",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Share your AI-generated art",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ImagePickerSection(
    selectedImagePath: String?,
    onPickImage: () -> Unit,
    isWideScreen: Boolean
) {
    val hasImage = selectedImagePath != null
    val height = if (isWideScreen) 300.dp else 220.dp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clickable(onClick = onPickImage),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasImage)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!hasImage) {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(20.dp)
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hasImage) {
                // Show selected image preview
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Image Selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = onPickImage) {
                        Text("Change Image")
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "iconPulse"
                    )
                    
                    Icon(
                        Icons.Outlined.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        text = "Tap to Select Image",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = "PNG, JPG up to 10MB\nRecommended: 1024x1024 or higher",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(icon, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            content()
        }
    }
}

@Composable
private fun GuidelinesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📋", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Submission Guidelines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            val guidelines = listOf(
                "✓ Only AI-generated art is allowed",
                "✓ Character must be from a book/novel",
                "✓ No NSFW or inappropriate content",
                "✓ You must have rights to share the image",
                "✓ Art will be reviewed before publishing"
            )
            
            guidelines.forEach { guideline ->
                Text(
                    text = guideline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

// Extension for dashed border (simplified)
private fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    shape: RoundedCornerShape
) = this.border(width, color, shape)

/**
 * Toggle between file picker and AI generation
 */
@Composable
private fun ImageSourceToggle(
    selectedMode: ImageSourceMode,
    onModeChange: (ImageSourceMode) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pick from device
            ToggleOption(
                icon = Icons.Outlined.Image,
                label = "Pick Image",
                emoji = "📁",
                isSelected = selectedMode == ImageSourceMode.PICK_FILE,
                onClick = { onModeChange(ImageSourceMode.PICK_FILE) },
                modifier = Modifier.weight(1f)
            )
            
            // Generate with AI
            ToggleOption(
                icon = Icons.Outlined.AutoAwesome,
                label = "Generate AI",
                emoji = "✨",
                isSelected = selectedMode == ImageSourceMode.GENERATE_AI,
                onClick = { onModeChange(ImageSourceMode.GENERATE_AI) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToggleOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Simple model info for UI display
 */
data class GeminiModelInfo(
    val id: String,
    val displayName: String,
    val description: String = ""
)

/**
 * Gemini AI image generation section
 */
@Composable
private fun GeminiGeneratorSection(
    apiKey: String,
    prompt: String,
    characterName: String,
    bookTitle: String,
    selectedStyle: String,
    selectedModel: GeminiModelInfo?,
    availableModels: List<GeminiModelInfo>,
    isLoadingModels: Boolean,
    generatedPreview: ByteArray?,
    isGenerating: Boolean,
    error: String?,
    onApiKeyClick: () -> Unit,
    onPromptChange: (String) -> Unit,
    onStyleChange: (String) -> Unit,
    onModelSelect: (GeminiModelInfo) -> Unit,
    onFetchModels: () -> Unit,
    onGenerate: () -> Unit,
    isWideScreen: Boolean
) {
    val height = if (isWideScreen) 400.dp else 320.dp
    val hasApiKey = apiKey.isNotBlank()
    var showModelDropdown by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = height),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✨", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gemini AI Generator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Create character art with AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // API Key status
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hasApiKey)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer,
                    onClick = onApiKeyClick
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (hasApiKey) Icons.Default.Key else Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (hasApiKey) "API Key Set" else "Add Key",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            // Model selector
            Text(
                text = "AI Model",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Box {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { 
                        if (hasApiKey) {
                            showModelDropdown = true
                            if (availableModels.isEmpty() || availableModels == defaultModels) {
                                onFetchModels()
                            }
                        } else {
                            onApiKeyClick()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedModel?.displayName ?: "Select Model",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (selectedModel?.description?.isNotBlank() == true) {
                                Text(
                                    text = selectedModel.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        if (isLoadingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select model"
                            )
                        }
                    }
                }
                
                DropdownMenu(
                    expanded = showModelDropdown,
                    onDismissRequest = { showModelDropdown = false }
                ) {
                    availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = model.displayName,
                                        fontWeight = if (model.id == selectedModel?.id) 
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (model.description.isNotBlank()) {
                                        Text(
                                            text = model.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onModelSelect(model)
                                showModelDropdown = false
                            },
                            leadingIcon = {
                                if (model.id == selectedModel?.id) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }
            
            // Style selector
            Text(
                text = "Art Style",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val styles = listOf(
                    "digital art" to "🎨",
                    "anime" to "🎌",
                    "realistic portrait" to "📷",
                    "fantasy illustration" to "🧙",
                    "watercolor" to "🖌️",
                    "oil painting" to "🖼️"
                )
                items(styles) { (style, emoji) ->
                    FilterChip(
                        selected = selectedStyle == style,
                        onClick = { onStyleChange(style) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji, fontSize = 12.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(style.replaceFirstChar { it.uppercase() })
                            }
                        }
                    )
                }
            }
            
            // Prompt input
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                label = { Text("Describe the character") },
                placeholder = { 
                    Text("e.g., Young wizard with round glasses, messy black hair, lightning scar on forehead...") 
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                enabled = !isGenerating
            )
            
            // Info text
            if (characterName.isBlank() || bookTitle.isBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Fill in Character Name and Book Title above first",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
            // Error message
            error?.let { errorMsg ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Generated preview or generate button
            if (generatedPreview != null) {
                // Show preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Image Generated!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${generatedPreview.size / 1024} KB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = onGenerate) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Regenerate")
                            }
                        }
                        // TODO: Show actual image preview with AsyncImage
                    }
                }
            } else {
                // Generate button
                Button(
                    onClick = onGenerate,
                    enabled = hasApiKey && prompt.isNotBlank() && 
                              characterName.isNotBlank() && bookTitle.isNotBlank() && 
                              !isGenerating,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Generating...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate Image")
                    }
                }
            }
        }
    }
}

/**
 * Dialog for entering Gemini API key
 */
@Composable
private fun GeminiApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf(currentKey) }
    var showKey by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔑", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text("Gemini API Key")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Enter your Gemini API key to generate images. Get one free at:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "aistudio.google.com/apikey",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("AIza...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showKey) 
                        androidx.compose.ui.text.input.VisualTransformation.None 
                    else 
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showKey) "Hide" else "Show"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                
                Text(
                    text = "⚠️ Your API key is stored locally and never shared.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(apiKey) },
                enabled = apiKey.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Overload for backward compatibility (without Gemini features)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadCharacterArtScreen(
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    onSubmit: (
        characterName: String,
        bookTitle: String,
        bookAuthor: String,
        description: String,
        aiModel: String,
        prompt: String,
        tags: List<String>
    ) -> Unit,
    selectedImagePath: String?,
    isUploading: Boolean,
    uploadProgress: Float,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {
    UploadCharacterArtScreen(
        onBack = onBack,
        onPickImage = onPickImage,
        onGenerateImage = { _, _, _, _, _ -> },
        onSubmit = onSubmit,
        selectedImagePath = selectedImagePath,
        generatedImagePreview = null,
        isUploading = isUploading,
        isGenerating = false,
        uploadProgress = uploadProgress,
        generationError = null,
        availableModels = defaultModels,
        selectedModel = defaultModels.firstOrNull(),
        isLoadingModels = false,
        onModelSelect = {},
        onFetchModels = {},
        onApiKeyChanged = {},
        initialApiKey = "",
        modifier = modifier,
        paddingValues = paddingValues
    )
}
