package ireader.presentation.ui.home.explore.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import ireader.domain.js.models.FilterDefinition
import ireader.domain.js.models.FilterOption
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Composable for rendering JavaScript plugin filters.
 * Displays different filter types based on FilterDefinition.
 */
@Composable
fun JSPluginFilters(
    filters: Map<String, FilterDefinition>,
    filterValues: Map<String, Any>,
    onFilterChanged: (String, Any) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        filters.forEach { (key, definition) ->
            when (definition) {
                is FilterDefinition.Picker -> {
                    JSPluginFilterPicker(
                        label = definition.label,
                        options = definition.options,
                        selectedValue = filterValues[key] as? String ?: definition.defaultValue,
                        onValueChanged = { value -> onFilterChanged(key, value) }
                    )
                }
                is FilterDefinition.TextInput -> {
                    JSPluginFilterTextInput(
                        label = definition.label,
                        value = filterValues[key] as? String ?: definition.defaultValue,
                        onValueChanged = { value -> onFilterChanged(key, value) }
                    )
                }
                is FilterDefinition.CheckboxGroup -> {
                    @Suppress("UNCHECKED_CAST")
                    val selectedValues = filterValues[key] as? List<String> ?: definition.defaultValues
                    JSPluginFilterCheckboxGroup(
                        label = definition.label,
                        options = definition.options,
                        selectedValues = selectedValues,
                        onValuesChanged = { values -> onFilterChanged(key, values) }
                    )
                }
                is FilterDefinition.ExcludableCheckboxGroup -> {
                    @Suppress("UNCHECKED_CAST")
                    val valueMap = filterValues[key] as? Map<String, List<String>>
                    val included = valueMap?.get("included") ?: definition.included
                    val excluded = valueMap?.get("excluded") ?: definition.excluded
                    JSPluginFilterExcludableCheckboxGroup(
                        label = definition.label,
                        options = definition.options,
                        includedValues = included,
                        excludedValues = excluded,
                        onValuesChanged = { inc, exc ->
                            onFilterChanged(key, mapOf("included" to inc, "excluded" to exc))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Picker filter - dropdown selection from predefined options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JSPluginFilterPicker(
    label: String,
    options: List<FilterOption>,
    selectedValue: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.find { it.value == selectedValue } ?: options.firstOrNull()
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption?.label ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onValueChanged(option.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Text input filter - free-text entry.
 */
@Composable
fun JSPluginFilterTextInput(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

/**
 * Checkbox group filter - multiple selection from options.
 */
@Composable
fun JSPluginFilterCheckboxGroup(
    label: String,
    options: List<FilterOption>,
    selectedValues: List<String>,
    onValuesChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = option.value in selectedValues,
                        onCheckedChange = { checked ->
                            val newValues = if (checked) {
                                selectedValues + option.value
                            } else {
                                selectedValues - option.value
                            }
                            onValuesChanged(newValues)
                        }
                    )
                    Text(
                        text = option.label,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Excludable checkbox group filter - tri-state selection (included/excluded/none).
 */
@Composable
fun JSPluginFilterExcludableCheckboxGroup(
    label: String,
    options: List<FilterOption>,
    includedValues: List<String>,
    excludedValues: List<String>,
    onValuesChanged: (List<String>, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val state = when {
                        option.value in includedValues -> ToggleableState.On
                        option.value in excludedValues -> ToggleableState.Indeterminate
                        else -> ToggleableState.Off
                    }
                    
                    TriStateCheckbox(
                        state = state,
                        onClick = {
                            val (newIncluded, newExcluded) = when (state) {
                                ToggleableState.Off -> {
                                    // Off -> Included
                                    Pair(
                                        includedValues + option.value,
                                        excludedValues
                                    )
                                }
                                ToggleableState.On -> {
                                    // Included -> Excluded
                                    Pair(
                                        includedValues - option.value,
                                        excludedValues + option.value
                                    )
                                }
                                ToggleableState.Indeterminate -> {
                                    // Excluded -> Off
                                    Pair(
                                        includedValues,
                                        excludedValues - option.value
                                    )
                                }
                            }
                            onValuesChanged(newIncluded, newExcluded)
                        }
                    )
                    
                    Text(
                        text = option.label,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    
                    // Show state indicator
                    when (state) {
                        ToggleableState.On -> {
                            Text(
                                text = localizeHelper.localize(Res.string.included),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        ToggleableState.Indeterminate -> {
                            Text(
                                text = localizeHelper.localize(Res.string.excluded),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
