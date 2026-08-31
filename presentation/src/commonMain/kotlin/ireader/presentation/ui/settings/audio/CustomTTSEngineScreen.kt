package ireader.presentation.ui.settings.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import ireader.domain.services.tts_service.*
import ireader.presentation.ui.component.IScaffold
import kotlinx.coroutines.launch

/**
 * Dedicated full-screen Custom TTS Engine Studio.
 * Replaces the cramped AlertDialog with an expansive, intuitive workspace for configuring
 * Hugging Face Spaces, Gradio endpoints, payload parameters, starter templates, and live testing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTTSEngineScreen(
    config: GradioTTSConfig,
    onDismiss: () -> Unit,
    onSave: (GradioTTSConfig) -> Unit,
    onAutoDetect: ((String, String?, (Result<GradioTTSConfig>) -> Unit) -> Unit)? = null,
    onTestCustomConfig: ((GradioTTSConfig, (Result<ByteArray>) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(config.name) }
    var spaceUrl by remember { mutableStateOf(config.spaceUrl) }
    var apiName by remember { mutableStateOf(config.apiName) }
    var apiKey by remember { mutableStateOf(config.apiKey ?: "") }
    var description by remember { mutableStateOf(config.description) }
    var audioOutputIndex by remember { mutableStateOf(config.audioOutputIndex.toString()) }
    var apiType by remember { mutableStateOf(config.apiType) }
    var parameters by remember { mutableStateOf(config.parameters) }

    // Auto-detect State
    var rawInputUrl by remember { mutableStateOf(config.spaceUrl) }
    var isAutoDetecting by remember { mutableStateOf(false) }
    var autoDetectMessage by remember { mutableStateOf<String?>(null) }
    var isAutoDetectSuccess by remember { mutableStateOf(false) }

    // Live Test Bench State
    var testSentence by remember { mutableStateOf("The quick brown fox jumps over the lazy dog.") }
    var isTestingVoice by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(false) }

    var showAdvanced by remember { mutableStateOf(false) }
    val isNew = config.spaceUrl.isEmpty()

    fun buildCurrentDraft(): GradioTTSConfig {
        return config.copy(
            name = name.ifBlank { "Custom TTS Space" },
            spaceUrl = spaceUrl,
            apiName = apiName.ifBlank { "predict" },
            apiKey = apiKey.ifBlank { null },
            description = description,
            audioOutputIndex = audioOutputIndex.toIntOrNull() ?: 0,
            parameters = parameters,
            apiType = apiType,
            isCustom = true
        )
    }

    IScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { scrollBehavior ->
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isNew) "Add Custom TTS Engine" else "Edit TTS Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hugging Face Space & Gradio Studio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (spaceUrl.isNotBlank()) {
                                onSave(buildCurrentDraft())
                                onDismiss()
                            }
                        },
                        enabled = spaceUrl.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Engine")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Smart Space Auto-Detection Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "1-Click Space Auto-Detect",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Paste any Hugging Face space URL or repository ID (e.g. hexgrad/Kokoro-82M). IReader will inspect its API endpoints, parameters, and audio stream format automatically.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = rawInputUrl,
                            onValueChange = {
                                rawInputUrl = it
                                spaceUrl = GradioSpaceDetector.normalizeSpaceUrl(it)
                            },
                            label = { Text("Hugging Face Space / URL") },
                            placeholder = { Text("e.g. hexgrad/Kokoro-82M or https://....hf.space") },
                            leadingIcon = {
                                Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (rawInputUrl.isBlank()) return@Button
                                isAutoDetecting = true
                                autoDetectMessage = "Inspecting Gradio space..."
                                isAutoDetectSuccess = false

                                val normalized = GradioSpaceDetector.normalizeSpaceUrl(rawInputUrl)
                                spaceUrl = normalized

                                if (onAutoDetect != null) {
                                    onAutoDetect(rawInputUrl, apiKey.ifBlank { null }) { result ->
                                        isAutoDetecting = false
                                        result.onSuccess { detected ->
                                            name = detected.name
                                            spaceUrl = detected.spaceUrl
                                            apiName = detected.apiName
                                            audioOutputIndex = detected.audioOutputIndex.toString()
                                            description = detected.description
                                            parameters = detected.parameters
                                            apiType = detected.apiType
                                            autoDetectMessage = "✓ Detected ${detected.parameters.size} parameter(s) on ${detected.apiName}"
                                            isAutoDetectSuccess = true
                                        }.onFailure { err ->
                                            autoDetectMessage = "Could not auto-detect: ${err.message ?: "Space offline"}. You can pick a starter template below."
                                            isAutoDetectSuccess = false
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        val client = HttpClient()
                                        val res = GradioSpaceDetector.detectSpace(client, rawInputUrl, apiKey.ifBlank { null })
                                        isAutoDetecting = false
                                        res.onSuccess { detected ->
                                            name = detected.name
                                            spaceUrl = detected.spaceUrl
                                            apiName = detected.apiName
                                            audioOutputIndex = detected.audioOutputIndex.toString()
                                            description = detected.description
                                            parameters = detected.parameters
                                            apiType = detected.apiType
                                            autoDetectMessage = "✓ Detected ${detected.parameters.size} parameter(s) on ${detected.apiName}"
                                            isAutoDetectSuccess = true
                                        }.onFailure { err ->
                                            autoDetectMessage = "Could not auto-detect: ${err.message ?: "Space offline"}. You can pick a starter template below."
                                            isAutoDetectSuccess = false
                                        }
                                    }
                                }
                            },
                            enabled = rawInputUrl.isNotBlank() && !isAutoDetecting,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (isAutoDetecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Inspecting Space Endpoints...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Auto-Detect Schema & Parameters")
                            }
                        }

                        AnimatedVisibility(visible = autoDetectMessage != null) {
                            autoDetectMessage?.let { msg ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isAutoDetectSuccess)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isAutoDetectSuccess)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Quick Starter Templates Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Starter Community Templates",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Choose a pre-configured architecture to auto-populate parameter mappings, speed multiplier, and voice selectors instantly:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GradioCommunityTemplates.TEMPLATES.forEach { template ->
                                val isSelected = name == template.name || (template.spaceUrl.isNotBlank() && spaceUrl == template.spaceUrl)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        name = template.name
                                        if (template.spaceUrl.isNotBlank()) {
                                            spaceUrl = template.spaceUrl
                                            rawInputUrl = template.spaceUrl
                                        }
                                        apiName = template.apiName
                                        audioOutputIndex = template.audioOutputIndex.toString()
                                        description = template.description
                                        apiType = template.apiType
                                        parameters = template.parameters
                                        autoDetectMessage = "Applied starter template: ${template.name}"
                                        isAutoDetectSuccess = true
                                    },
                                    label = {
                                        Text(
                                            text = template.name
                                                .replace(" Neural TTS", "")
                                                .replace(" Multilingual Cloning", "")
                                                .replace(" Expressive TTS", ""),
                                            maxLines = 1
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Engine Identity & Credentials Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Engine Identity & Access",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            placeholder = { Text("e.g. Kokoro 82M Space") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = spaceUrl,
                            onValueChange = { spaceUrl = it },
                            label = { Text("Direct Endpoint URL") },
                            placeholder = { Text("https://username-space.hf.space") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("Hugging Face Access Token (Optional)") },
                            placeholder = { Text("Required only for private spaces (hf_...)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (Optional)") },
                            placeholder = { Text("Voice characteristics, quality notes, etc.") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // 4. Live Voice Synthesis Test Bench Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Live Voice Test Bench",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        OutlinedTextField(
                            value = testSentence,
                            onValueChange = { testSentence = it },
                            label = { Text("Sample Text") },
                            placeholder = { Text("Enter text to synthesize...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                isTestingVoice = true
                                testResultText = "Sending test synthesis request..."
                                isTestSuccess = false

                                val draftConfig = buildCurrentDraft()

                                if (onTestCustomConfig != null) {
                                    onTestCustomConfig(draftConfig) { result ->
                                        isTestingVoice = false
                                        result.onSuccess {
                                            testResultText = "✓ Voice synthesis succeeded! Space returned valid audio."
                                            isTestSuccess = true
                                        }.onFailure { err ->
                                            testResultText = "✕ Synthesis failed: ${err.message}"
                                            isTestSuccess = false
                                        }
                                    }
                                } else {
                                    testResultText = "Ready to test in Audio Studio."
                                    isTestingVoice = false
                                    isTestSuccess = true
                                }
                            },
                            enabled = spaceUrl.isNotBlank() && !isTestingVoice,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (isTestingVoice) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing Audio...")
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Voice Synthesis")
                            }
                        }

                        AnimatedVisibility(visible = testResultText != null) {
                            testResultText?.let { txt ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isTestSuccess)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = txt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isTestSuccess)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Payload Parameters Studio Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "API Payload Parameters (${parameters.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Mapped in sequential order of API arguments",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                parameters = parameters + GradioParam(
                                    name = "param_${parameters.size + 1}",
                                    type = GradioParamType.STRING,
                                    defaultValue = ""
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Parameter", maxLines = 1)
                        }

                        if (parameters.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No parameters mapped yet. Auto-detect or select a starter template above.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Render individual parameters
            itemsIndexed(parameters, key = { index, param -> "${param.name}-$index" }) { index, param ->
                ParameterEditorCard(
                    index = index + 1,
                    param = param,
                    onUpdate = { newParam ->
                        parameters = parameters.toMutableList().apply {
                            set(index, newParam)
                        }
                    },
                    onRemove = {
                        parameters = parameters.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                )
            }

            // 6. Advanced Endpoint Settings Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Advanced Endpoint Mappings",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(onClick = { showAdvanced = !showAdvanced }) {
                                Icon(
                                    imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }
                        }

                        if (showAdvanced) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            OutlinedTextField(
                                value = apiName,
                                onValueChange = { apiName = it },
                                label = { Text("API Name / Path") },
                                placeholder = { Text("/predict or /generate") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = audioOutputIndex,
                                onValueChange = { audioOutputIndex = it.filter { c -> c.isDigit() } },
                                label = { Text("Audio Output Component Index") },
                                placeholder = { Text("0") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            var apiTypeExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = apiTypeExpanded,
                                onExpandedChange = { apiTypeExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = apiType.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Gradio Endpoint Type") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = apiTypeExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = apiTypeExpanded,
                                    onDismissRequest = { apiTypeExpanded = false }
                                ) {
                                    GradioApiType.entries.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.name) },
                                            onClick = {
                                                apiType = type
                                                apiTypeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Bottom Action Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (spaceUrl.isNotBlank()) {
                                onSave(buildCurrentDraft())
                                onDismiss()
                            }
                        },
                        enabled = spaceUrl.isNotBlank(),
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNew) "Add Engine" else "Save Changes")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

/**
 * Individual parameter editor card with type selector and default values
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParameterEditorCard(
    index: Int,
    param: GradioParam,
    onUpdate: (GradioParam) -> Unit,
    onRemove: () -> Unit
) {
    var typeExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "#$index",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = param.name.ifBlank { "Parameter $index" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Parameter",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = param.name,
                    onValueChange = { onUpdate(param.copy(name = it)) },
                    label = { Text("Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = param.type.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        GradioParamType.entries.forEach { paramType ->
                            DropdownMenuItem(
                                text = { Text(paramType.name) },
                                onClick = {
                                    onUpdate(param.copy(type = paramType))
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = param.defaultValue ?: "",
                onValueChange = { onUpdate(param.copy(defaultValue = it)) },
                label = { Text("Default Value") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            if (param.type == GradioParamType.CHOICE) {
                OutlinedTextField(
                    value = param.choices?.joinToString(", ") ?: "",
                    onValueChange = { raw ->
                        val list = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onUpdate(param.copy(choices = list))
                    },
                    label = { Text("Options (comma-separated)") },
                    placeholder = { Text("af_heart, af_bella, am_michael") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}
