package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.ChipChoicePreference
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.components.SwitchPreference
import ireader.presentation.ui.component.components.setupUiComponent
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.Colour
import ireader.presentation.ui.core.ui.PreferenceMutableState

@Composable
fun TranslationSettingsScreen(
    modifier: Modifier = Modifier,
    translatorEngine: Long,
    openAIApiKey:
    PreferenceMutableState<String>,
    deepSeekApiKey: PreferenceMutableState<String>,
    ollamaUrl: PreferenceMutableState<String>,
    ollamaModel: PreferenceMutableState<String>,
    translatorContentType: PreferenceMutableState<Int>,
    translatorToneType: Int,
    translatorPreserveStyle: PreferenceMutableState<Boolean>,
    onTranslatorEngineChange: (Long) -> Unit,
    onOpenAIApiKeyChange: (String) -> Unit,
    onDeepSeekApiKeyChange: (String) -> Unit,
    onOllamaUrlChange: (String) -> Unit,
    onOllamaModelChange: (String) -> Unit,
    onTranslatorContentTypeChange: (Int) -> Unit,
    onTranslatorToneTypeChange: (Int) -> Unit,
    onTranslatorPreserveStyleChange: (Boolean) -> Unit,
    translationEnginesManager: TranslationEnginesManager,
) {
    // This state is used to force recomposition
    var recomposeCounter by remember { mutableStateOf(0) }
    val forceRecompose = { recomposeCounter += 1 }
    val navigator = cafe.adriel.voyager.navigator.LocalNavigator.currentOrThrow
    val scrollState = rememberScrollState()
    val engines = translationEnginesManager.getAvailableEngines()
    val options = engines.map { it.id to it.engineName }
    var openAIKeyVisible by remember { mutableStateOf(false) }
    //Temp
    var deepSeekKeyVisible by remember { mutableStateOf(false) }
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val contentTypes = ContentType.entries.map {
        it.ordinal to it.name.lowercase()
            .replaceFirstChar { c -> c.uppercase() }
    }
    val toneTypes = ToneType.entries.map {
        it.ordinal to it.name.lowercase()
            .replaceFirstChar { c -> c.uppercase() }
    }
    
    // Determine if the current engine is AI-powered
    val currentEngine = engines.find { it.id == translatorEngine }
    val isAiEngine = currentEngine?.supportsContextAwareTranslation == true
    
    val items = mutableListOf<Components>()
    
    // Info card about translation engines
    items.add(Components.Dynamic {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = localizeHelper.localize(MR.strings.translation_settings),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Select your preferred translation engine and configure its settings. AI-powered engines (OpenAI, DeepSeek) require API keys and offer more advanced features.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    })
    
    // Engine Selection Section
    items.add(Components.Header(
        localizeHelper.localize(MR.strings.translation_engine))
    )
    
    items.add(Components.Chip(
        preference = options.map { it.second },
        title = localizeHelper.localize(MR.strings.translation_engine),
        onValueChange = { value ->
            onTranslatorEngineChange(value.toLong())
            forceRecompose()
        },
        selected = translatorEngine.toInt(),
    ))
    
    items.add(Components.Space)
    
    // API Keys Section (only show if an engine requiring API keys is selected)
    if (engines.find { it.id == translatorEngine }?.requiresApiKey == true) {
        items.add(Components.Header(
            localizeHelper.localize(MR.strings.api_keys))
        )
        
        if (translatorEngine == 2L) { // OpenAI
            items.add(Components.Row(
                title = localizeHelper.localize(MR.strings.openai_api_key),
                icon = Icons.Default.Api,
                subtitle = "Required for OpenAI translations",
                onClick = {},
                action = {
                    OutlinedTextField(
                        value = openAIApiKey.value,
                        onValueChange = { 
                            onOpenAIApiKeyChange(it)
                            forceRecompose()
                        },
                        label = { Text(localizeHelper.localize(MR.strings.openai_api_key)) },
                        visualTransformation = if (openAIKeyVisible) {
                            androidx.compose.ui.text.input.VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            Components.Companion.VisibilityIcon(
                                visible = openAIKeyVisible,
                                onVisibilityChanged = { openAIKeyVisible = it }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        )
                    )
                },
            ))
        }
        
        if (translatorEngine == 3L) { // DeepSeek
            items.add(Components.Row(
                title = localizeHelper.localize(MR.strings.deepseek_api_key),
                icon = Icons.Default.Api,
                subtitle = "Required for DeepSeek translations",
                onClick = {},
                action = {
                    OutlinedTextField(
                        value = deepSeekApiKey.value,
                        onValueChange = { 
                            onDeepSeekApiKeyChange(it)
                            forceRecompose()
                        },
                        label = { Text(localizeHelper.localize(MR.strings.deepseek_api_key)) },
                        visualTransformation = if (deepSeekKeyVisible) {
                            androidx.compose.ui.text.input.VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            Components.Companion.VisibilityIcon(
                                visible = deepSeekKeyVisible,
                                onVisibilityChanged = { deepSeekKeyVisible = it }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        )
                    )
                },
            ))
        }
        
        items.add(Components.Space)
    }
    
    // Add Ollama settings section if that engine is selected
    if (translatorEngine == 5L) { // Ollama
        items.add(Components.Header(
            "Ollama Configuration")
        )
        
        items.add(Components.Row(
            title = localizeHelper.localize(MR.strings.ollama_url),
            icon = Icons.Default.Api,
            subtitle = "URL of your Ollama API server",
            onClick = {},
            action = {
                OutlinedTextField(
                    value = ollamaUrl.value,
                    onValueChange = { 
                        onOllamaUrlChange(it)
                        forceRecompose()
                    },
                    label = { Text(localizeHelper.localize(MR.strings.ollama_url)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri
                    )
                )
            },
        ))
        
        items.add(Components.Row(
            title = localizeHelper.localize(MR.strings.ollama_model),
            icon = Icons.Default.Settings,
            subtitle = "Model to use for translation (e.g., mistral, llama2)",
            onClick = {},
            action = {
                OutlinedTextField(
                    value = ollamaModel.value,
                    onValueChange = { 
                        onOllamaModelChange(it)
                        forceRecompose()
                    },
                    label = { Text(localizeHelper.localize(MR.strings.ollama_model)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    )
                )
            },
        ))
        
        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ollama Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ollama is a local LLM server for translations. Make sure Ollama is installed and running on your device or network. You need to pull the model first with 'ollama pull modelname'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        })
        
        items.add(Components.Space)
    }
    
    // Add LibreTranslate info section if that engine is selected
    if (translatorEngine == 4L) { // LibreTranslate
        items.add(Components.Header(
            "LibreTranslate Information")
        )
        
        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LibreTranslate",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = localizeHelper.localize(MR.strings.libretranslate_info),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        })
        
        items.add(Components.Space)
    }

    
    // Add Webscraping AI translation section if that engine is selected
    if (translatorEngine == 6L) { // WebscrapingTranslateEngine
        items.add(Components.Header(
            "ChatGPT WebView Translation")
        )
        
        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ChatGPT Login Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "This translation engine uses a WebView to access ChatGPT directly. You need to sign in to ChatGPT with your account to use this feature. The app will save your cookies for future translations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "If a CAPTCHA appears, you will need to complete it manually.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Button(
                        onClick = {
                            // Navigate to ChatGpt login screen
                            navigator.push(
                                ireader.presentation.core.ui.ChatGptLoginScreenSpec()
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Text("Sign in to ChatGPT")
                    }
                }
            }
        })
        
        items.add(Components.Space)
    }
    
    // Advanced Settings Section
    if (isAiEngine) {
        items.add(Components.Header(
            localizeHelper.localize(MR.strings.advanced_settings))
        )
        
        items.add(Components.Chip(
            title = localizeHelper.localize(MR.strings.content_type),
            icon = Icons.Default.Translate,
            subtitle = "Optimizes translation for specific content",
            onValueChange = { 
                onTranslatorContentTypeChange(it)
                forceRecompose()
            },
            selected = translatorContentType.value,
            preference = contentTypes.map { it.second }
        ))
        
        items.add(Components.Chip(
            title = localizeHelper.localize(MR.strings.tone_type),
            icon = Icons.Default.Translate,
            subtitle = "Sets the tone of translated text",
            onValueChange = { 
                onTranslatorToneTypeChange(it)
                forceRecompose()
            },
            selected = translatorToneType,
            preference = toneTypes.map { it.second }
        ))
        
        items.add(Components.Switch(
            title = localizeHelper.localize(MR.strings.preserve_style),
            icon = Icons.Default.Style,
            subtitle = "Keep the original writing style when translating",
            preference = translatorPreserveStyle,
        ))
    } else {
        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Advanced Features",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Switch to an AI-powered engine (OpenAI or DeepSeek) to access advanced translation settings like content type, tone control, and style preservation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        })
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        modifier = modifier.fillMaxSize()
    ) {
        setupUiComponent(list = items)
    }
} 