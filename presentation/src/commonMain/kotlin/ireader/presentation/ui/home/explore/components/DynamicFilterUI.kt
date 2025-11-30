package ireader.presentation.ui.home.explore.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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

/**
 * Composable that dynamically generates filter UI based on filter definitions.
 * Supports text, select, checkbox, tristate, and sort filters.
 * 
 * @param filterDefinitions Map of filter ID to FilterDefinition
 * @param filterValues Current filter values
 * @param onFilterChange Callback when a filter value changes
 * @param modifier Modifier for the root composable
 */
@Composable
fun DynamicFilterUI(
    filterDefinitions: Map<String, FilterDefinition>,
    filterValues: Map<String, Any>,
    onFilterChange: (String, Any) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        filterDefinitions.forEach { (filterId, definition) ->
            when (definition) {
                is FilterDefinition.TextInput -> {
                    TextInputFilter(
                        label = definition.label,
                        value = filterValues[filterId] as? String ?: definition.defaultValue,
                        onValueChange = { onFilterChange(filterId, it) }
                    )
                }
                is FilterDefinition.Picker -> {
                    PickerFilter(
                        label = definition.label,
                        options = definition.options,
                        selectedValue = filterValues[filterId] as? String ?: definition.defaultValue,
                        onValueChange = { onFilterChange(filterId, it) }
                    )
                }
                is FilterDefinition.CheckboxGroup -> {
                    CheckboxGroupFilter(
                        label = definition.label,
                        options = definition.options,
                        selectedValues = filterValues[filterId] as? List<String> ?: definition.defaultValues,
                        onValueChange = { onFilterChange(filterId, it) }
                    )
                }
                is FilterDefinition.ExcludableCheckboxGroup -> {
                    ExcludableCheckboxGroupFilter(
                        label = definition.label,
                        options = definition.options,
                        included = (filterValues[filterId] as? Map<*, *>)?.get("included") as? List<String> ?: definition.included,
                        excluded = (filterValues[filterId] as? Map<*, *>)?.get("excluded") as? List<String> ?: definition.excluded,
                        onValueChange = { included, excluded ->
                            onFilterChange(filterId, mapOf("included" to included, "excluded" to excluded))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Text input filter component.
 */
@Composable
private fun TextInputFilter(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

/**
 * Picker (dropdown) filter component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerFilter(
    label: String,
    options: List<FilterOption>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.find { it.value == selectedValue } ?: options.firstOrNull()
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedOption?.label ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = localizeHelper.localize(Res.string.dropdown)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onValueChange(option.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Checkbox group filter component.
 */
@Composable
private fun CheckboxGroupFilter(
    label: String,
    options: List<FilterOption>,
    selectedValues: List<String>,
    onValueChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedValues.contains(option.value),
                    onCheckedChange = { checked ->
                        val newValues = if (checked) {
                            selectedValues + option.value
                        } else {
                            selectedValues - option.value
                        }
                        onValueChange(newValues)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Excludable checkbox group filter component (tristate).
 * Supports three states: included, excluded, and neither.
 */
@Composable
private fun ExcludableCheckboxGroupFilter(
    label: String,
    options: List<FilterOption>,
    included: List<String>,
    excluded: List<String>,
    onValueChange: (List<String>, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TriStateCheckbox(
                    state = when {
                        included.contains(option.value) -> ToggleableState.On
                        excluded.contains(option.value) -> ToggleableState.Off
                        else -> ToggleableState.Indeterminate
                    },
                    onClick = {
                        val newIncluded: List<String>
                        val newExcluded: List<String>
                        
                        when {
                            // Currently included -> move to excluded
                            included.contains(option.value) -> {
                                newIncluded = included - option.value
                                newExcluded = excluded + option.value
                            }
                            // Currently excluded -> move to neither
                            excluded.contains(option.value) -> {
                                newIncluded = included
                                newExcluded = excluded - option.value
                            }
                            // Currently neither -> move to included
                            else -> {
                                newIncluded = included + option.value
                                newExcluded = excluded
                            }
                        }
                        
                        onValueChange(newIncluded, newExcluded)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        included.contains(option.value) -> MaterialTheme.colorScheme.primary
                        excluded.contains(option.value) -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
        
        // Legend
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap to cycle: Include → Exclude → None",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
