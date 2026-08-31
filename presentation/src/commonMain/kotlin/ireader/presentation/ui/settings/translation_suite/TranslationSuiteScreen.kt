package ireader.presentation.ui.settings.translation_suite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.models.entities.TextReplacement
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.general.MlKitInitState
import ireader.presentation.ui.settings.general.TestConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSuiteScreen(
    state: TranslationSuiteState,
    onNavigateUp: () -> Unit,
    onSelectEngineId: (Long) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onOpenAIApiKeyChange: (String) -> Unit = {},
    onDeepSeekApiKeyChange: (String) -> Unit = {},
    onGeminiApiKeyChange: (String) -> Unit = {},
    onGeminiModelChange: (String) -> Unit = {},
    onRefreshGeminiModels: () -> Unit = {},
    onClaudeApiKeyChange: (String) -> Unit = {},
    onClaudeModelChange: (String) -> Unit = {},
    onOpenRouterApiKeyChange: (String) -> Unit = {},
    onOpenRouterModelChange: (String) -> Unit = {},
    onLoadOpenRouterModels: () -> Unit = {},
    onNvidiaApiKeyChange: (String) -> Unit = {},
    onNvidiaModelChange: (String) -> Unit = {},
    onLoadNvidiaModels: () -> Unit = {},
    onOllamaUrlChange: (String) -> Unit = {},
    onOllamaModelChange: (String) -> Unit = {},
    onTestConnection: () -> Unit = {},
    onResetTestConnectionState: () -> Unit = {},
    onInitializeGoogleMlKit: (String, String) -> Unit = { _, _ -> },
    onNavigateToLogin: ((String) -> Unit)? = null,
    // Context & Style
    onContentTypeChange: (ContentType) -> Unit = {},
    onToneTypeChange: (ToneType) -> Unit = {},
    onPreserveStyleChange: (Boolean) -> Unit = {},
    onCustomPromptChange: (String) -> Unit = {},
    onToggleAutoTranslateChapters: (Boolean) -> Unit = {},
    onToggleAutoTranslateNovelNames: (Boolean) -> Unit = {},
    onToggleAutoShareTranslations: (Boolean) -> Unit = {},
    onContributorNameChange: (String) -> Unit = {},
    // Glossary
    onGlossarySearch: (String) -> Unit = {},
    onAddGlossaryTerm: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteGlossaryTerm: (Long) -> Unit = {},
    onSetShowAddGlossaryDialog: (Boolean) -> Unit = {},
    // Text Cleanup
    onTogglePreset: (String, Boolean) -> Unit = { _, _ -> },
    onSetShowAddRuleDialog: (Boolean) -> Unit = {},
    onAddCustomReplacement: (String, String, Boolean) -> Unit = { _, _, _ -> },
    onDeleteCustomReplacement: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Engines & Models", "Context & Style", "Glossary", "Text Cleanup")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.testConnectionState) {
        when (val result = state.testConnectionState) {
            is TestConnectionState.Success -> {
                snackbarHostState.showSnackbar(result.message)
                onResetTestConnectionState()
            }
            is TestConnectionState.Error -> {
                snackbarHostState.showSnackbar(result.message)
                onResetTestConnectionState()
            }
            else -> {}
        }
    }

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = "Translation & Text Suite",
                popBackStack = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            // Tab 0: Engines & Models
            if (selectedTab == 0) {
                item {
                    EngineSelectionHeader(
                        state = state,
                        onSelectEngineId = onSelectEngineId
                    )
                }

                item {
                    ActiveEngineDetailCard(
                        state = state,
                        onOpenAIApiKeyChange = onOpenAIApiKeyChange,
                        onDeepSeekApiKeyChange = onDeepSeekApiKeyChange,
                        onGeminiApiKeyChange = onGeminiApiKeyChange,
                        onGeminiModelChange = onGeminiModelChange,
                        onRefreshGeminiModels = onRefreshGeminiModels,
                        onClaudeApiKeyChange = onClaudeApiKeyChange,
                        onClaudeModelChange = onClaudeModelChange,
                        onOpenRouterApiKeyChange = onOpenRouterApiKeyChange,
                        onOpenRouterModelChange = onOpenRouterModelChange,
                        onLoadOpenRouterModels = onLoadOpenRouterModels,
                        onNvidiaApiKeyChange = onNvidiaApiKeyChange,
                        onNvidiaModelChange = onNvidiaModelChange,
                        onLoadNvidiaModels = onLoadNvidiaModels,
                        onOllamaUrlChange = onOllamaUrlChange,
                        onOllamaModelChange = onOllamaModelChange,
                        onTestConnection = onTestConnection,
                        onInitializeGoogleMlKit = onInitializeGoogleMlKit,
                        onNavigateToLogin = onNavigateToLogin
                    )
                }
            }

            // Tab 1: Context & Style
            if (selectedTab == 1) {
                item {
                    ContextAndStyleSection(
                        state = state,
                        onContentTypeChange = onContentTypeChange,
                        onToneTypeChange = onToneTypeChange,
                        onPreserveStyleChange = onPreserveStyleChange,
                        onCustomPromptChange = onCustomPromptChange,
                        onToggleAutoTranslateChapters = onToggleAutoTranslateChapters,
                        onToggleAutoTranslateNovelNames = onToggleAutoTranslateNovelNames,
                        onToggleAutoShareTranslations = onToggleAutoShareTranslations,
                        onContributorNameChange = onContributorNameChange
                    )
                }
            }

            // Tab 2: Glossary Dictionary
            if (selectedTab == 2) {
                item {
                    GlossaryHeaderSection(
                        searchQuery = state.glossarySearchQuery,
                        onSearchChange = onGlossarySearch,
                        onAddClick = { onSetShowAddGlossaryDialog(true) }
                    )
                }

                val filteredTerms = if (state.glossarySearchQuery.isBlank()) {
                    state.glossaryTerms
                } else {
                    state.glossaryTerms.filter {
                        it.sourceTerm.contains(state.glossarySearchQuery, ignoreCase = true) ||
                        it.targetTerm.contains(state.glossarySearchQuery, ignoreCase = true)
                    }
                }

                if (filteredTerms.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Translate,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No glossary terms yet",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Add names, locations, and special terms to ensure consistent translation across chapters.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTerms, key = { it.id }) { term ->
                        GlossaryTermCard(
                            term = term,
                            onDelete = { onDeleteGlossaryTerm(term.id) }
                        )
                    }
                }
            }

            // Tab 3: Text Cleanup & Rules
            if (selectedTab == 3) {
                item {
                    TextCleanupPresetsSection(
                        presets = state.activePresets,
                        onTogglePreset = onTogglePreset
                    )
                }

                item {
                    CustomReplacementRulesHeader(
                        onAddRuleClick = { onSetShowAddRuleDialog(true) }
                    )
                }

                if (state.textReplacements.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Rule,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No custom replacement rules",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Add custom regex or plain text find & replace patterns to clean up novel text before reading or translating.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(state.textReplacements, key = { it.id }) { rule ->
                        CustomReplacementRuleCard(
                            rule = rule,
                            onDelete = { onDeleteCustomReplacement(rule.id) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        // Add Glossary Dialog
        if (state.showAddGlossaryDialog) {
            AddGlossaryDialog(
                onDismiss = { onSetShowAddGlossaryDialog(false) },
                onConfirm = { src, tgt, notes -> onAddGlossaryTerm(src, tgt, notes) }
            )
        }

        // Add Replacement Rule Dialog
        if (state.showAddRuleDialog) {
            AddReplacementRuleDialog(
                onDismiss = { onSetShowAddRuleDialog(false) },
                onConfirm = { find, replace, isRegex -> onAddCustomReplacement(find, replace, isRegex) }
            )
        }
    }
}

@Composable
private fun EngineSelectionHeader(
    state: TranslationSuiteState,
    onSelectEngineId: (Long) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Translation Engine",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val defaultEngineNames = listOf(
                11L to "Google Translate (Free)",
                8L to "Gemini AI",
                3L to "DeepSeek AI",
                2L to "OpenAI (ChatGPT)",
                13L to "Claude AI",
                9L to "OpenRouter AI",
                10L to "NVIDIA NIM",
                5L to "Ollama (Local LLM)",
                0L to "Google ML Kit (Offline)"
            )

            val engines = if (state.availableEngines.isNotEmpty()) {
                state.availableEngines.map { it.id to it.engineName }
            } else {
                defaultEngineNames
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(engines) { (id, name) ->
                    val isSelected = state.selectedEngineId == id
                    val isPluginEngine = id !in listOf(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectEngineId(id) },
                        label = { Text(name, maxLines = 1) },
                        leadingIcon = if (isPluginEngine) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Extension,
                                    contentDescription = "Plugin Engine",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveEngineDetailCard(
    state: TranslationSuiteState,
    onOpenAIApiKeyChange: (String) -> Unit,
    onDeepSeekApiKeyChange: (String) -> Unit,
    onGeminiApiKeyChange: (String) -> Unit,
    onGeminiModelChange: (String) -> Unit,
    onRefreshGeminiModels: () -> Unit,
    onClaudeApiKeyChange: (String) -> Unit,
    onClaudeModelChange: (String) -> Unit,
    onOpenRouterApiKeyChange: (String) -> Unit,
    onOpenRouterModelChange: (String) -> Unit,
    onLoadOpenRouterModels: () -> Unit,
    onNvidiaApiKeyChange: (String) -> Unit,
    onNvidiaModelChange: (String) -> Unit,
    onLoadNvidiaModels: () -> Unit,
    onOllamaUrlChange: (String) -> Unit,
    onOllamaModelChange: (String) -> Unit,
    onTestConnection: () -> Unit,
    onInitializeGoogleMlKit: (String, String) -> Unit,
    onNavigateToLogin: ((String) -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (state.selectedEngineId) {
                13L -> {
                    // Claude AI (Anthropic)
                    Text("Claude AI (Anthropic) Configuration", style = MaterialTheme.typography.titleSmall)
                    ApiKeyField(
                        value = state.claudeApiKey,
                        onValueChange = onClaudeApiKeyChange,
                        label = "Anthropic API Key (sk-ant-...)"
                    )
                    OutlinedTextField(
                        value = if (state.claudeModel.isNotBlank()) state.claudeModel else "claude-3-5-sonnet-20241022",
                        onValueChange = onClaudeModelChange,
                        label = { Text("Model Name (e.g. claude-3-5-sonnet-20241022, claude-3-haiku-20240307)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                8L -> {
                    // Gemini AI
                    Text("Google Gemini Configuration", style = MaterialTheme.typography.titleSmall)
                    ApiKeyField(
                        value = state.geminiApiKey,
                        onValueChange = onGeminiApiKeyChange,
                        label = "Gemini API Key"
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = if (state.geminiModel.isNotBlank()) state.geminiModel else "gemini-2.0-flash",
                            onValueChange = onGeminiModelChange,
                            label = { Text("Model Name") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onRefreshGeminiModels) {
                            Icon(Icons.Default.Refresh, contentDescription = "Fetch Models")
                        }
                    }
                }
                3L -> {
                    // DeepSeek
                    Text("DeepSeek Configuration", style = MaterialTheme.typography.titleSmall)
                    ApiKeyField(
                        value = state.deepSeekApiKey,
                        onValueChange = onDeepSeekApiKeyChange,
                        label = "DeepSeek API Key"
                    )
                    onNavigateToLogin?.let { onLogin ->
                        OutlinedButton(onClick = { onLogin("deepseek") }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Login with DeepSeek Web Session")
                        }
                    }
                }
                2L -> {
                    // OpenAI
                    Text("OpenAI Configuration", style = MaterialTheme.typography.titleSmall)
                    ApiKeyField(
                        value = state.openAIApiKey,
                        onValueChange = onOpenAIApiKeyChange,
                        label = "OpenAI API Key"
                    )
                    onNavigateToLogin?.let { onLogin ->
                        OutlinedButton(onClick = { onLogin("chatgpt") }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Login with ChatGPT Web Session")
                        }
                    }
                }
                9L -> {
                    // OpenRouter
                    Text("OpenRouter Configuration", style = MaterialTheme.typography.titleSmall)
                    ApiKeyField(
                        value = state.openRouterApiKey,
                        onValueChange = onOpenRouterApiKeyChange,
                        label = "OpenRouter API Key"
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = if (state.openRouterModel.isNotBlank()) state.openRouterModel else "google/gemini-2.0-flash-exp:free",
                            onValueChange = onOpenRouterModelChange,
                            label = { Text("Model Name") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onLoadOpenRouterModels) {
                            Icon(Icons.Default.Refresh, contentDescription = "Fetch Models")
                        }
                    }
                }
                10L -> {
                    // NVIDIA NIM
                    Text("NVIDIA NIM Configuration", style = MaterialTheme.typography.titleSmall)
                    ApiKeyField(
                        value = state.nvidiaApiKey,
                        onValueChange = onNvidiaApiKeyChange,
                        label = "NVIDIA API Key"
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = if (state.nvidiaModel.isNotBlank()) state.nvidiaModel else "meta/llama-3.1-70b-instruct",
                            onValueChange = onNvidiaModelChange,
                            label = { Text("Model Name") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onLoadNvidiaModels) {
                            Icon(Icons.Default.Refresh, contentDescription = "Fetch Models")
                        }
                    }
                }
                5L -> {
                    // Ollama
                    Text("Ollama (Local LLM) Configuration", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = state.ollamaUrl,
                        onValueChange = onOllamaUrlChange,
                        label = { Text("Ollama Server URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.ollamaModel,
                        onValueChange = onOllamaModelChange,
                        label = { Text("Model Name (e.g. llama3, qwen)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                0L -> {
                    // Google ML Kit
                    Text("Google ML Kit (Offline Translation)", style = MaterialTheme.typography.titleSmall)
                    Text("Downloads language models directly onto the device for fast, private offline translation.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = { onInitializeGoogleMlKit("en", "es") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (state.mlKitInitState is MlKitInitState.Initializing) "Downloading (${state.mlKitInitProgress}%)..." else "Download Offline Language Pack")
                    }
                }
                11L -> {
                    // Google Translate Free / Default
                    Text("Online Free Translation", style = MaterialTheme.typography.titleSmall)
                    Text("Ready to use out of the box with zero setup or API keys required.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    val pluginEngine = state.availableEngines.find { it.id == state.selectedEngineId }
                    if (pluginEngine != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(pluginEngine.engineName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Plugin Engine",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Translation engine provided by an installed plugin. Supports ${pluginEngine.supportedLanguages.size} language pair(s).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    } else {
                        Text("Custom Translation Engine", style = MaterialTheme.typography.titleSmall)
                        Text("Active translation engine configured and ready.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Connection Test",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onTestConnection) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (state.testConnectionState is TestConnectionState.Testing) "Testing..." else "Test Connection")
                }
            }
        }
    }
}

@Composable
private fun ApiKeyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ContextAndStyleSection(
    state: TranslationSuiteState,
    onContentTypeChange: (ContentType) -> Unit,
    onToneTypeChange: (ToneType) -> Unit,
    onPreserveStyleChange: (Boolean) -> Unit,
    onCustomPromptChange: (String) -> Unit,
    onToggleAutoTranslateChapters: (Boolean) -> Unit,
    onToggleAutoTranslateNovelNames: (Boolean) -> Unit,
    onToggleAutoShareTranslations: (Boolean) -> Unit,
    onContributorNameChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Translation Context & Tone", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            // Content Type
            Text("Content Type", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    ContentType.GENERAL to "General",
                    ContentType.LITERARY to "Literary",
                    ContentType.CREATIVE to "Creative",
                    ContentType.CONVERSATION to "Dialogue"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = state.contentType == type,
                        onClick = { onContentTypeChange(type) },
                        label = { Text(label) }
                    )
                }
            }

            // Tone
            Text("Tone", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    ToneType.NEUTRAL to "Neutral",
                    ToneType.FORMAL to "Formal",
                    ToneType.CASUAL to "Casual",
                    ToneType.HUMOROUS to "Humorous"
                ).forEach { (tone, label) ->
                    FilterChip(
                        selected = state.toneType == tone,
                        onClick = { onToneTypeChange(tone) },
                        label = { Text(label) }
                    )
                }
            }

            // Preserve Style
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Preserve Novel Style", style = MaterialTheme.typography.bodyLarge)
                    Text("Maintains line breaks, quotes, and punctuation cadence", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.preserveStyle, onCheckedChange = onPreserveStyleChange)
            }

            // Custom Prompt
            OutlinedTextField(
                value = state.customPrompt,
                onValueChange = onCustomPromptChange,
                label = { Text("Custom System Prompt (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            HorizontalDivider()

            // Automation
            Text("Automation", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Translate Novel Titles", style = MaterialTheme.typography.bodyLarge)
                    Text("Translate untranslated novel names in explore & library", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.autoTranslateNovelNames, onCheckedChange = onToggleAutoTranslateNovelNames)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Translate Next Chapters", style = MaterialTheme.typography.bodyLarge)
                    Text("Pre-translate chapters in background while reading", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.autoTranslateChapters, onCheckedChange = onToggleAutoTranslateChapters)
            }
        }
    }
}

@Composable
private fun GlossaryHeaderSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Glossary Dictionary", style = MaterialTheme.typography.titleMedium)
                Text("Force exact translations for names and world-building terms", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Term")
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search glossary terms...") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun GlossaryTermCard(
    term: GlossaryTermItem,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(term.sourceTerm, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.padding(horizontal = 6.dp).size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(term.targetTerm, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                if (term.notes.isNotBlank()) {
                    Text(term.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TextCleanupPresetsSection(
    presets: List<TextCleanupPreset>,
    onTogglePreset: (String, Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("One-Click Cleanup Presets", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text("Automatically strips recurring noise, aggregator watermarks, and ads from novels.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            presets.forEach { preset ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(preset.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(preset.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = preset.isEnabled, onCheckedChange = { onTogglePreset(preset.id, it) })
                }
            }
        }
    }
}

@Composable
private fun CustomReplacementRulesHeader(
    onAddRuleClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Custom Replacement Rules", style = MaterialTheme.typography.titleMedium)
            Text("Regex & plain text find/replace rules", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = onAddRuleClick) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Rule")
        }
    }
}

@Composable
private fun CustomReplacementRuleCard(
    rule: TextReplacement,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(rule.findText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("➔ \"${rule.replaceText}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddGlossaryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var source by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Glossary Term") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source Term (e.g. 萧炎)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Translation (e.g. Xiao Yan)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (source.isNotBlank() && target.isNotBlank()) {
                        onConfirm(source, target, notes)
                    }
                },
                enabled = source.isNotBlank() && target.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddReplacementRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit
) {
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var isRegex by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Replacement Rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    label = { Text("Find Pattern") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    label = { Text("Replace With") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRegex, onCheckedChange = { isRegex = it })
                    Text("Treat as Regular Expression (Regex)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (findText.isNotBlank()) {
                        onConfirm(findText, replaceText, isRegex)
                    }
                },
                enabled = findText.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


