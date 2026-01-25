package ireader.presentation.ui.home.tts

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ireader.domain.preferences.models.prefs.IReaderVoice
import ireader.domain.usecases.preferences.TextReaderPrefUseCase
import ireader.domain.usecases.tts.GetNativeTTSVoicesUseCase
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.theme.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.Locale

/**
 * Native TTS Voice Selection Screen for Android
 * 
 * Displays available voices and languages from the device's native TTS engine.
 * Only shown when Native TTS engine is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeTTSVoiceSelectionScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val scope = rememberCoroutineScope()
    
    val getNativeTTSVoicesUseCase = remember { GetNativeTTSVoicesUseCase(context) }
    val textReaderPrefUseCase: TextReaderPrefUseCase = koinInject()
    
    var availableLanguages by remember { mutableStateOf<List<Locale>>(emptyList()) }
    var availableVoices by remember { mutableStateOf<List<IReaderVoice>>(emptyList()) }
    var selectedLanguage by remember { mutableStateOf<Locale?>(null) }
    var selectedVoice by remember { mutableStateOf<IReaderVoice?>(null) }
    var pitch by remember { mutableStateOf(1.0f) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load saved preferences
    LaunchedEffect(Unit) {
        selectedVoice = textReaderPrefUseCase.readVoice()
        val savedLanguage = textReaderPrefUseCase.readLanguage()
        pitch = textReaderPrefUseCase.readPitch()
        
        // Load available languages
        getNativeTTSVoicesUseCase.getAvailableLanguages()
            .onSuccess { languages ->
                availableLanguages = languages.sortedBy { it.displayName }
                
                // Set selected language from saved preference or default
                selectedLanguage = if (savedLanguage.isNotEmpty()) {
                    languages.find { it.language == savedLanguage } ?: Locale.getDefault()
                } else {
                    Locale.getDefault()
                }
                
                // Load voices for selected language
                selectedLanguage?.let { locale ->
                    loadVoicesForLanguage(locale, getNativeTTSVoicesUseCase) { voices ->
                        availableVoices = voices
                        isLoading = false
                    }
                }
            }
            .onFailure { error ->
                errorMessage = error.message
                isLoading = false
            }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = localizeHelper.localize(Res.string.voice_selection),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // Language Selector
                    LanguageSelector(
                        languages = availableLanguages,
                        selectedLanguage = selectedLanguage,
                        onLanguageSelected = { locale ->
                            selectedLanguage = locale
                            isLoading = true
                            scope.launch {
                                loadVoicesForLanguage(locale, getNativeTTSVoicesUseCase) { voices ->
                                    availableVoices = voices
                                    isLoading = false
                                }
                                textReaderPrefUseCase.saveLanguage(locale.language)
                            }
                        },
                        localizeHelper = localizeHelper
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Voice List
                    VoiceList(
                        voices = availableVoices,
                        selectedVoice = selectedVoice,
                        onVoiceSelected = { voice ->
                            selectedVoice = voice
                            scope.launch {
                                textReaderPrefUseCase.saveVoice(voice)
                            }
                        },
                        localizeHelper = localizeHelper,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Pitch Slider
                    PitchSlider(
                        pitch = pitch,
                        onPitchChange = { newPitch ->
                            pitch = newPitch
                            scope.launch {
                                textReaderPrefUseCase.savePitch(newPitch)
                            }
                        },
                        localizeHelper = localizeHelper
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(localizeHelper.localize(Res.string.close))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector(
    languages: List<Locale>,
    selectedLanguage: Locale?,
    onLanguageSelected: (Locale) -> Unit,
    localizeHelper: ireader.i18n.LocalizeHelper,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            text = localizeHelper.localize(Res.string.language),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedLanguage?.displayName ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languages.forEach { locale ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = locale.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onLanguageSelected(locale)
                            expanded = false
                        },
                        leadingIcon = if (locale == selectedLanguage) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceList(
    voices: List<IReaderVoice>,
    selectedVoice: IReaderVoice?,
    onVoiceSelected: (IReaderVoice) -> Unit,
    localizeHelper: ireader.i18n.LocalizeHelper,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = localizeHelper.localize(Res.string.available_voices),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (voices.isEmpty()) {
            Text(
                text = localizeHelper.localize(Res.string.no_voices_available_for_this_language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(voices, key = { it.name }) { voice ->
                    VoiceItem(
                        voice = voice,
                        isSelected = voice.name == selectedVoice?.name,
                        onSelected = { onVoiceSelected(voice) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceItem(
    voice: IReaderVoice,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.small,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voice.localDisplayName.ifEmpty { voice.name },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (voice.country.isNotEmpty()) {
                    Text(
                        text = "${voice.language}-${voice.country}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private suspend fun loadVoicesForLanguage(
    locale: Locale,
    getNativeTTSVoicesUseCase: GetNativeTTSVoicesUseCase,
    onResult: (List<IReaderVoice>) -> Unit
) {
    getNativeTTSVoicesUseCase.getVoicesForLanguage(locale)
        .onSuccess { voices ->
            onResult(voices.sortedBy { it.localDisplayName.ifEmpty { it.name } })
        }
        .onFailure {
            onResult(emptyList())
        }
}


@Composable
private fun PitchSlider(
    pitch: Float,
    onPitchChange: (Float) -> Unit,
    localizeHelper: ireader.i18n.LocalizeHelper,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizeHelper.localize(Res.string.pitch),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format("%.2f", pitch),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Slider(
            value = pitch,
            onValueChange = onPitchChange,
            valueRange = 0.5f..2.0f,
            steps = 29, // 0.05 increments
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0.5",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "2.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
