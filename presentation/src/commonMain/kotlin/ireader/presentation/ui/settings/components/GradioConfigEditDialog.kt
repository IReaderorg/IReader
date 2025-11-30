package ireader.presentation.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.services.tts_service.GradioParam
import ireader.domain.services.tts_service.GradioParamType
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Dialog for editing Gradio TTS configurations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradioConfigEditDialog(
    config: GradioTTSConfig,
    onDismiss: () -> Unit,
    onSave: (GradioTTSConfig) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var name by remember { mutableStateOf(config.name) }
    var spaceUrl by remember { mutableStateOf(config.spaceUrl) }
    var apiName by remember { mutableStateOf(config.apiName) }
    var apiKey by remember { mutableStateOf(config.apiKey ?: "") }
    var description by remember { mutableStateOf(config.description) }
    var audioOutputIndex by remember { mutableStateOf(config.audioOutputIndex.toString()) }
    var parameters by remember { mutableStateOf(config.parameters) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (config.isCustom && config.spaceUrl.isEmpty()) 
                    "New TTS Engine" 
                else 
                    "Edit TTS Engine"
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(localizeHelper.localize(Res.string.name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = spaceUrl,
                        onValueChange = { spaceUrl = it },
                        label = { Text(localizeHelper.localize(Res.string.space_url)) },
                        placeholder = { Text("https://username-space.hf.space") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = apiName,
                        onValueChange = { apiName = it },
                        label = { Text(localizeHelper.localize(Res.string.api_name)) },
                        placeholder = { Text(localizeHelper.localize(Res.string.predict_or_synthesize_speech)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text(localizeHelper.localize(Res.string.api_key_optional)) },
                        placeholder = { Text(localizeHelper.localize(Res.string.for_private_spaces)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(localizeHelper.localize(Res.string.description)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = audioOutputIndex,
                        onValueChange = { audioOutputIndex = it.filter { c -> c.isDigit() } },
                        label = { Text(localizeHelper.localize(Res.string.audio_output_index)) },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                // Parameters section
                item {
                    Text(
                        text = localizeHelper.localize(Res.string.parameters),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                itemsIndexed(parameters) { index, param ->
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
                
                item {
                    OutlinedButton(
                        onClick = {
                            parameters = parameters + GradioParam(
                                name = "param${parameters.size + 1}",
                                type = GradioParamType.STRING,
                                defaultValue = ""
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.add_parameter))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(config.copy(
                        name = name,
                        spaceUrl = spaceUrl,
                        apiName = apiName,
                        apiKey = apiKey.ifEmpty { null },
                        description = description,
                        audioOutputIndex = audioOutputIndex.toIntOrNull() ?: 0,
                        parameters = parameters,
                        isCustom = true
                    ))
                },
                enabled = name.isNotBlank() && spaceUrl.isNotBlank()
            ) {
                Text(localizeHelper.localize(Res.string.save))
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
                    label = { Text(localizeHelper.localize(Res.string.name)) },
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
                    label = { Text(localizeHelper.localize(Res.string.type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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
            
            // Default value
            OutlinedTextField(
                value = param.defaultValue ?: "",
                onValueChange = { onUpdate(param.copy(defaultValue = it)) },
                label = { Text(localizeHelper.localize(Res.string.default_value)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Special flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = param.isTextInput,
                        onCheckedChange = { 
                            onUpdate(param.copy(
                                isTextInput = it, 
                                isSpeedInput = if (it) false else param.isSpeedInput
                            )) 
                        }
                    )
                    Text("Text Input", style = MaterialTheme.typography.bodySmall)
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = param.isSpeedInput,
                        onCheckedChange = { 
                            onUpdate(param.copy(
                                isSpeedInput = it, 
                                isTextInput = if (it) false else param.isTextInput
                            )) 
                        }
                    )
                    Text("Speed Input", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
