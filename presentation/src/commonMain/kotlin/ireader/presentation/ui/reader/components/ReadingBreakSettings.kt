package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Settings component for reading break reminders
 * Can be added to reader settings or general settings
 */
@Composable
fun ReadingBreakSettings(
    enabled: Boolean,
    intervalMinutes: Int,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = localizeHelper.localize(Res.string.reading_break),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = localizeHelper.localize(Res.string.reading_break_reminder),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Enable/Disable Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localizeHelper.localize(Res.string.enable_reminders),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = localizeHelper.localize(Res.string.get_gentle_reminders_to_rest_your_eyes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
        
        // Interval Selection (only shown when enabled)
        if (enabled) {
            Divider()
            
            Text(
                text = localizeHelper.localize(Res.string.reminder_interval),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            val intervals = listOf(30, 45, 60, 90, 120)
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                intervals.forEach { interval ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = intervalMinutes == interval,
                            onClick = { onIntervalChange(interval) }
                        )
                        Text(
                            text = "$interval minutes",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
            
            // Info text
            Text(
                text = localizeHelper.localize(Res.string.youll_receive_a_gentle_reminder),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Compact version for use in bottom sheets or dialogs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingBreakSettingsCompact(
    enabled: Boolean,
    intervalMinutes: Int,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Enable/Disable
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizeHelper.localize(Res.string.reading_break_reminder),
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
        
        // Interval dropdown (only shown when enabled)
        if (enabled) {
            var expanded by remember { mutableStateOf(false) }
            val intervals = listOf(30, 45, 60, 90, 120)
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = "$intervalMinutes minutes",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(localizeHelper.localize(Res.string.interval)) },
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
                    intervals.forEach { interval ->
                        DropdownMenuItem(
                            text = { Text("$interval minutes") },
                            onClick = {
                                onIntervalChange(interval)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
