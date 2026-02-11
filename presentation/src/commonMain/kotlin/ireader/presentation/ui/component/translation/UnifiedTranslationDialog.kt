package ireader.presentation.ui.component.translation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ireader.domain.services.common.TranslationProgress
import ireader.domain.services.common.TranslationStatus
import ireader.domain.services.common.ServiceState
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Translation mode - determines the context of translation
 */
enum class TranslationMode {
    SINGLE_CHAPTER,    // Single chapter in reader
    MASS_CHAPTERS,     // Multiple chapters in detail screen
    TTS_CHAPTER        // Chapter being read by TTS
}

/**
 * Translation dialog state holder
 */
@Stable
class TranslationDialogState {
    var isVisible by mutableStateOf(false)
    var mode by mutableStateOf(TranslationMode.SINGLE_CHAPTER)
    var chapterCount by mutableStateOf(1)
    var chapterIds by mutableStateOf<List<Long>>(emptyList())
    var bookId by mutableStateOf<Long?>(null)
    var selectedEngineId by mutableStateOf(-1L)
    var sourceLanguage by mutableStateOf("en")
    var targetLanguage by mutableStateOf("en")
    
    // Progress state
    var isTranslating by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var totalChapters by mutableStateOf(0)
    var completedChapters by mutableStateOf(0)
    var currentChapterName by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    
    // Warning state
    var showWarning by mutableStateOf(false)
    var warningMessage by mutableStateOf("")
    var estimatedTimeMinutes by mutableStateOf(0L)
    
    fun show(
        mode: TranslationMode,
        chapterIds: List<Long>,
        bookId: Long?,
        defaultEngineId: Long = -1L,
        defaultSourceLang: String = "en",
        defaultTargetLang: String = "en"
    ) {
        this.mode = mode
        this.chapterIds = chapterIds
        this.chapterCount = chapterIds.size
        this.bookId = bookId
        this.selectedEngineId = defaultEngineId
        this.sourceLanguage = defaultSourceLang
        this.targetLanguage = defaultTargetLang
        this.isVisible = true
        this.isTranslating = false
        this.showWarning = false
        this.errorMessage = null
    }
    
    fun hide() {
        isVisible = false
        isTranslating = false
        showWarning = false
    }
    
    fun reset() {
        hide()
        chapterIds = emptyList()
        chapterCount = 1
        bookId = null
        completedChapters = 0
        totalChapters = 0
        currentChapterName = ""
        errorMessage = null
    }
}

@Composable
fun rememberTranslationDialogState(): TranslationDialogState {
    return remember { TranslationDialogState() }
}

/**
 * Unified Translation Dialog - Modern UI for all translation contexts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedTranslationDialog(
    state: TranslationDialogState,
    availableEngines: List<TranslationEngine>,
    onTranslate: (engineId: Long, sourceLang: String, targetLang: String, bypassWarning: Boolean) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    if (!state.isVisible) return
    
    Dialog(
        onDismissRequest = { if (!state.isTranslating) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !state.isTranslating,
            dismissOnClickOutside = !state.isTranslating,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            AnimatedContent(
                targetState = when {
                    state.showWarning -> "warning"
                    state.isTranslating -> "progress"
                    else -> "options"
                },
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
                },
                label = localizeHelper.localize(Res.string.dialog_content)
            ) { screen ->
                when (screen) {
                    "warning" -> TranslationWarningContent(
                        state = state,
                        onConfirm = { onTranslate(state.selectedEngineId, state.sourceLanguage, state.targetLanguage, true) },
                        onCancel = { state.showWarning = false },
                        onNavigateToSettings = onNavigateToSettings
                    )
                    "progress" -> TranslationProgressContent(
                        state = state,
                        onPause = onPause,
                        onResume = onResume,
                        onCancel = onCancel
                    )
                    else -> TranslationOptionsContent(
                        state = state,
                        availableEngines = availableEngines,
                        onTranslate = { onTranslate(state.selectedEngineId, state.sourceLanguage, state.targetLanguage, false) },
                        onNavigateToSettings = onNavigateToSettings,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslationOptionsContent(
    state: TranslationDialogState,
    availableEngines: List<TranslationEngine>,
    onTranslate: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var expandedEngine by remember { mutableStateOf(false) }
    var expandedSource by remember { mutableStateOf(false) }
    var expandedTarget by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = localizeHelper.localize(Res.string.translate_action),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (state.mode) {
                            TranslationMode.SINGLE_CHAPTER -> "Current chapter"
                            TranslationMode.MASS_CHAPTERS -> "${state.chapterCount} chapters"
                            TranslationMode.TTS_CHAPTER -> "TTS chapter"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = localizeHelper.localize(Res.string.close))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Engine Selection
        TranslationDropdown(
            label = localizeHelper.localize(Res.string.translation_engine),
            icon = Icons.Outlined.Memory,
            value = availableEngines.find { it.id == state.selectedEngineId }?.name ?: "Select Engine",
            expanded = expandedEngine,
            onExpandedChange = { expandedEngine = it },
            items = availableEngines,
            onItemSelected = { engine ->
                state.selectedEngineId = engine.id
                expandedEngine = false
            },
            itemContent = { engine ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(engine.name, style = MaterialTheme.typography.bodyMedium)
                        if (engine.isOffline) {
                            Text(
                                "Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (engine.requiresApiKey) {
                        Icon(
                            Icons.Outlined.Key,
                            contentDescription = localizeHelper.localize(Res.string.requires_api_key),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Language Selection Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Source Language
            Box(modifier = Modifier.weight(1f)) {
                LanguageDropdown(
                    label = localizeHelper.localize(Res.string.from),
                    value = getLanguageName(state.sourceLanguage),
                    expanded = expandedSource,
                    onExpandedChange = { expandedSource = it },
                    onLanguageSelected = { code ->
                        state.sourceLanguage = code
                        expandedSource = false
                    }
                )
            }
            
            // Swap button
            IconButton(
                onClick = {
                    val temp = state.sourceLanguage
                    state.sourceLanguage = state.targetLanguage
                    state.targetLanguage = temp
                },
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = localizeHelper.localize(Res.string.swap_languages),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Target Language
            Box(modifier = Modifier.weight(1f)) {
                LanguageDropdown(
                    label = localizeHelper.localize(Res.string.to),
                    value = getLanguageName(state.targetLanguage),
                    expanded = expandedTarget,
                    onExpandedChange = { expandedTarget = it },
                    onLanguageSelected = { code ->
                        state.targetLanguage = code
                        expandedTarget = false
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Info card for mass translation
        if (state.mode == TranslationMode.MASS_CHAPTERS && state.chapterCount > 5) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.large_translations_may_take_time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Settings button
            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.settings))
            }
            
            // Translate button
            Button(
                onClick = onTranslate,
                modifier = Modifier.weight(1.5f),
                contentPadding = PaddingValues(vertical = 12.dp),
                enabled = state.selectedEngineId >= 0
            ) {
                Icon(
                    Icons.Default.Translate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.translate_action))
            }
        }
    }
}

@Composable
private fun TranslationWarningContent(
    state: TranslationDialogState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Warning icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = localizeHelper.localize(Res.string.rate_limit_warning),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = state.warningMessage.ifEmpty {
                "Translating ${state.chapterCount} chapters may exhaust API credits or result in IP blocking."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (state.estimatedTimeMinutes > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estimated time: ~${state.estimatedTimeMinutes} minutes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Bypass info
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = localizeHelper.localize(Res.string.enable_bypass_warning_in_settings),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(localizeHelper.localize(Res.string.continues))
            }
        }
    }
}

@Composable
private fun TranslationProgressContent(
    state: TranslationDialogState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val progress = if (state.totalChapters > 0) {
        state.completedChapters.toFloat() / state.totalChapters
    } else 0f
    
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated progress indicator
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 8.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (state.isPaused) "Paused" else "Translating...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${state.completedChapters} / ${state.totalChapters} chapters",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (state.currentChapterName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.currentChapterName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        state.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.cancel))
            }
            
            Button(
                onClick = if (state.isPaused) onResume else onPause,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.isPaused) "Resume" else "Pause")
            }
        }
    }
}

// Helper composables and data classes continue in next part


/**
 * Translation engine data class
 */
data class TranslationEngine(
    val id: Long,
    val name: String,
    val isOffline: Boolean = false,
    val requiresApiKey: Boolean = false,
    val requiresRateLimiting: Boolean = false,
    val isPlugin: Boolean = false,
    val pluginId: String? = null
)

/**
 * Built-in translation engines
 * Additional engines are available as plugins from the Feature Store
 */
object TranslationEngines {
    // Only built-in engines - plugins are loaded dynamically
    val BUILT_IN = listOf(
        TranslationEngine(0L, "Google ML Kit", isOffline = true),
        TranslationEngine(8L, "Gemini API", requiresApiKey = true, requiresRateLimiting = true),
        TranslationEngine(9L, "OpenRouter AI", requiresApiKey = true, requiresRateLimiting = true)
    )
    
    // For backward compatibility - returns only built-in engines
    // Use TranslationEnginesManager.getAvailableEngines() for full list including plugins
    val ALL: List<TranslationEngine> get() = BUILT_IN
    
    fun getById(id: Long): TranslationEngine? = BUILT_IN.find { it.id == id }
}

/**
 * All supported languages for translation (Google ML Kit compatible)
 */
private val COMMON_LANGUAGES = listOf(
    "af" to "Afrikaans",
    "sq" to "Albanian",
    "ar" to "Arabic",
    "be" to "Belarusian",
    "bn" to "Bengali",
    "bg" to "Bulgarian",
    "ca" to "Catalan",
    "zh" to "Chinese",
    "hr" to "Croatian",
    "cs" to "Czech",
    "da" to "Danish",
    "nl" to "Dutch",
    "en" to "English",
    "eo" to "Esperanto",
    "et" to "Estonian",
    "fi" to "Finnish",
    "fr" to "French",
    "gl" to "Galician",
    "ka" to "Georgian",
    "de" to "German",
    "el" to "Greek",
    "gu" to "Gujarati",
    "ht" to "Haitian Creole",
    "he" to "Hebrew",
    "hi" to "Hindi",
    "hu" to "Hungarian",
    "is" to "Icelandic",
    "id" to "Indonesian",
    "ga" to "Irish",
    "it" to "Italian",
    "ja" to "Japanese",
    "kn" to "Kannada",
    "ko" to "Korean",
    "lv" to "Latvian",
    "lt" to "Lithuanian",
    "mk" to "Macedonian",
    "mr" to "Marathi",
    "ms" to "Malay",
    "mt" to "Maltese",
    "no" to "Norwegian",
    "fa" to "Persian",
    "pl" to "Polish",
    "pt" to "Portuguese",
    "ro" to "Romanian",
    "ru" to "Russian",
    "sr" to "Serbian",
    "sk" to "Slovak",
    "sl" to "Slovenian",
    "es" to "Spanish",
    "sw" to "Swahili",
    "sv" to "Swedish",
    "tl" to "Tagalog",
    "ta" to "Tamil",
    "te" to "Telugu",
    "th" to "Thai",
    "tr" to "Turkish",
    "uk" to "Ukrainian",
    "ur" to "Urdu",
    "vi" to "Vietnamese",
    "cy" to "Welsh"
)

private fun getLanguageName(code: String): String {
    return COMMON_LANGUAGES.find { it.first == code }?.second ?: code.uppercase()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> TranslationDropdown(
    label: String,
    icon: ImageVector,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    itemContent: @Composable (T) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { itemContent(item) },
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                COMMON_LANGUAGES.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = { onLanguageSelected(code) }
                    )
                }
            }
        }
    }
}

/**
 * Quick translate button that shows the dialog on long press
 * Can be used in any screen (detail, reader, TTS)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateIconButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(localizeHelper.localize(Res.string.long_press_for_options))
            }
        },
        state = rememberTooltipState()
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = localizeHelper.localize(Res.string.translate_action),
                tint = if (enabled) tint else tint.copy(alpha = 0.38f)
            )
        }
    }
}
