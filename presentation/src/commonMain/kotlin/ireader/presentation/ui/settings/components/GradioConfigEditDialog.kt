package ireader.presentation.ui.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import ireader.domain.services.tts_service.GradioApiType
import ireader.domain.services.tts_service.GradioCommunityTemplates
import ireader.domain.services.tts_service.GradioParam
import ireader.domain.services.tts_service.GradioParamType
import ireader.domain.services.tts_service.GradioSpaceDetector
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
import kotlinx.coroutines.launch

/**
 * Smart Dialog for adding and editing Gradio Cloud TTS configurations.
 * Features 1-click Hugging Face Space auto-detection, popular community architecture templates,
 * live voice test bench, and visual parameter scaffolding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradioConfigEditDialog(
    config: GradioTTSConfig,
    onDismiss: () -> Unit,
    onSave: (GradioTTSConfig) -> Unit,
    onAutoDetect: ((String, String?, (Result<GradioTTSConfig>) -> Unit) -> Unit)? = null,
    onTestCustomConfig: ((GradioTTSConfig, (Result<ByteArray>) -> Unit) -> Unit)? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(config.name) }
    var spaceUrl by remember { mutableStateOf(config.spaceUrl) }
    var apiName by remember { mutableStateOf(config.apiName) }
    var apiKey by remember { mutableStateOf(config.apiKey ?: "") }
    var description by remember { mutableStateOf(config.description) }
    var audioOutputIndex by remember { mutableStateOf(config.audioOutputIndex.toString()) }
    var apiType by remember { mutableStateOf(config.apiType) }
    var parameters by remember { mutableStateOf(config.parameters) }
    var availableEndpoints by remember { mutableStateOf(config.availableEndpoints) }

    // Auto-detect & Test State
    var rawInputUrl by remember { mutableStateOf(config.spaceUrl) }
    var isAutoDetecting by remember { mutableStateOf(false) }
    var autoDetectMessage by remember { mutableStateOf<String?>(null) }
    var isAutoDetectSuccess by remember { mutableStateOf(false) }

    var isTestingVoice by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(false) }

    var showAdvanced by remember { mutableStateOf(false) }

    val isNew = config.spaceUrl.isEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isNew) "Add Custom TTS Space" else "Edit TTS Space",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // 1. Quick Community Architecture Templates
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Quick Starter Templates",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GradioCommunityTemplates.TEMPLATES.forEach { template ->
                                SuggestionChip(
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
                                        autoDetectMessage = "Applied template: ${template.name}"
                                        isAutoDetectSuccess = true
                                    },
                                    label = {
                                        Text(
                                            text = template.name.replace(" Neural TTS", "").replace(" Multilingual Cloning", "").replace(" Expressive TTS", ""),
                                            maxLines = 1
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Smart Auto-Detection Bar
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "✨ Smart Space Auto-Detect",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Paste any Hugging Face space URL or ID (e.g. hexgrad/Kokoro-TTS). We will inspect its API endpoints, parameters, and audio stream automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = rawInputUrl,
                                    onValueChange = {
                                        rawInputUrl = it
                                        spaceUrl = GradioSpaceDetector.normalizeSpaceUrl(it)
                                    },
                                    label = { Text("Hugging Face Space / URL") },
                                    placeholder = { Text("username/space-name") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
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
                                                    availableEndpoints = detected.availableEndpoints
                                                    autoDetectMessage = "✓ Detected endpoint ${detected.apiName} with ${detected.parameters.size} parameter(s)${if (detected.availableEndpoints.size > 1) " (${detected.availableEndpoints.size} endpoints found)" else ""}"
                                                    isAutoDetectSuccess = true
                                                }.onFailure { err ->
                                                    autoDetectMessage = "Could not auto-detect: ${err.message ?: "Space offline"}. You can pick a starter template above."
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
                                                    availableEndpoints = detected.availableEndpoints
                                                    autoDetectMessage = "✓ Detected endpoint ${detected.apiName} with ${detected.parameters.size} parameter(s)${if (detected.availableEndpoints.size > 1) " (${detected.availableEndpoints.size} endpoints found)" else ""}"
                                                    isAutoDetectSuccess = true
                                                }.onFailure { err ->
                                                    autoDetectMessage = "Could not auto-detect: ${err.message ?: "Space offline"}. You can pick a starter template above."
                                                    isAutoDetectSuccess = false
                                                }
                                            }
                                        }
                                    },
                                    enabled = rawInputUrl.isNotBlank() && !isAutoDetecting
                                ) {
                                    if (isAutoDetecting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text("Auto Detect")
                                    }
                                }
                            }

                            AnimatedVisibility(visible = autoDetectMessage != null) {
                                autoDetectMessage?.let { msg ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isAutoDetectSuccess)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = msg,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isAutoDetectSuccess)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Engine Display Name & Space URL
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = spaceUrl,
                        onValueChange = { spaceUrl = it },
                        label = { Text("Direct Endpoint URL") },
                        placeholder = { Text("https://username-space.hf.space") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("Hugging Face Token / API Key (Optional)") },
                        placeholder = { Text("For private spaces (hf_...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                // 4. Live Test Bench
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
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
                                Text(
                                    text = "🔊 Test Connection",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                FilledTonalButton(
                                    onClick = {
                                        isTestingVoice = true
                                        testResultText = "Testing voice synthesis..."
                                        isTestSuccess = false

                                        val draftConfig = config.copy(
                                            name = name,
                                            spaceUrl = spaceUrl,
                                            apiName = apiName,
                                            apiKey = apiKey.ifBlank { null },
                                            description = description,
                                            audioOutputIndex = audioOutputIndex.toIntOrNull() ?: 0,
                                            parameters = parameters,
                                            apiType = apiType,
                                            isCustom = true
                                        )

                                        if (onTestCustomConfig != null) {
                                            onTestCustomConfig(draftConfig) { result ->
                                                isTestingVoice = false
                                                result.onSuccess {
                                                    testResultText = "✓ Voice synthesis succeeded! Space is ready."
                                                    isTestSuccess = true
                                                }.onFailure { err ->
                                                    testResultText = "✕ Synthesis failed: ${err.message}"
                                                    isTestSuccess = false
                                                }
                                            }
                                        } else {
                                            testResultText = "Ready to save and test in Audio Studio."
                                            isTestingVoice = false
                                            isTestSuccess = true
                                        }
                                    },
                                    enabled = spaceUrl.isNotBlank() && !isTestingVoice
                                ) {
                                    if (isTestingVoice) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Test Voice")
                                    }
                                }
                            }

                            AnimatedVisibility(visible = testResultText != null) {
                                testResultText?.let { txt ->
                                    Text(
                                        text = txt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isTestSuccess)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Parameters Section Header
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Parameters (${parameters.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        FilledTonalButton(
                            onClick = {
                                parameters = parameters + GradioParam(
                                    name = "param${parameters.size + 1}",
                                    type = GradioParamType.STRING,
                                    defaultValue = ""
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Parameter", maxLines = 1)
                        }
                    }
                }

                itemsIndexed(parameters, key = { index, param -> "${param.name}-$index" }) { index, param ->
                    ParameterEditor(
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

                // 6. Advanced Settings Accordion
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth().clickable { showAdvanced = !showAdvanced }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Advanced Endpoint Settings", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(
                                if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }
                }

                if (availableEndpoints.isNotEmpty()) {
                    item {
                        Text(
                            text = "Discovered Space Endpoints (${availableEndpoints.size}):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableEndpoints.forEach { ep ->
                                FilterChip(
                                    selected = apiName == ep,
                                    onClick = { apiName = ep },
                                    label = { Text(ep) }
                                )
                            }
                        }
                    }
                }

                if (showAdvanced) {
                    item {
                        OutlinedTextField(
                            value = apiName,
                            onValueChange = { apiName = it },
                            label = { Text("API Name / Endpoint") },
                            placeholder = { Text("/predict or /text_to_speech") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = audioOutputIndex,
                            onValueChange = { audioOutputIndex = it.filter { c -> c.isDigit() } },
                            label = { Text("Audio Output Component Index") },
                            placeholder = { Text("0") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        var apiTypeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = apiTypeExpanded,
                            onExpandedChange = { apiTypeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = apiType.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("API Protocol Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = apiTypeExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        config.copy(
                            name = name.ifBlank { "Custom Space" },
                            spaceUrl = spaceUrl,
                            apiName = if (apiName.startsWith("/")) apiName else if (apiName.isNotBlank()) "/$apiName" else "/predict",
                            apiKey = apiKey.ifEmpty { null },
                            description = description,
                            audioOutputIndex = audioOutputIndex.toIntOrNull() ?: 0,
                            parameters = parameters,
                            apiType = apiType,
                            isCustom = true,
                            availableEndpoints = availableEndpoints
                        )
                    )
                },
                enabled = spaceUrl.isNotBlank()
            ) {
                Text("Save Voice Engine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParameterEditor(
    param: GradioParam,
    onUpdate: (GradioParam) -> Unit,
    onRemove: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                OutlinedTextField(
                    value = param.name,
                    onValueChange = { onUpdate(param.copy(name = it)) },
                    label = { Text("Param Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = localizeHelper.localize(Res.string.remove),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Type selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = param.type.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Parameter Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    GradioParamType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                onUpdate(param.copy(type = type))
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Default value or choices
            val choices = param.choices
            if (param.type == GradioParamType.CHOICE && choices != null) {
                Text(
                    text = "Choices (${choices.size}): ${choices.take(5).joinToString(", ")}${if (choices.size > 5) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = param.defaultValue ?: "",
                onValueChange = { onUpdate(param.copy(defaultValue = it)) },
                label = { Text("Default Value") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Special flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = param.isTextInput,
                    onClick = {
                        onUpdate(param.copy(
                            isTextInput = !param.isTextInput,
                            isSpeedInput = if (!param.isTextInput) false else param.isSpeedInput
                        ))
                    },
                    label = { Text("Receives Text") },
                    leadingIcon = {
                        if (param.isTextInput) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )

                FilterChip(
                    selected = param.isSpeedInput,
                    onClick = {
                        onUpdate(param.copy(
                            isSpeedInput = !param.isSpeedInput,
                            isTextInput = if (!param.isSpeedInput) false else param.isTextInput
                        ))
                    },
                    label = { Text("Receives Speed") },
                    leadingIcon = {
                        if (param.isSpeedInput) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
        }
    }
}
