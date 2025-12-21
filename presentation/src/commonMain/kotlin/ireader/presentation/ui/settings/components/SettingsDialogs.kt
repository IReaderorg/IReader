package ireader.presentation.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.components.IAlertDialog
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
import ireader.presentation.ui.core.theme.currentOrThrow

/**
 * Unified settings dialog components with consistent Material3 styling.
 */

/**
 * Standard confirmation dialog for destructive actions.
 * 
 * @param title The dialog title
 * @param message The confirmation message
 * @param confirmText Text for the confirm button (default: "Confirm")
 * @param dismissText Text for the dismiss button (default: "Cancel")
 * @param icon Optional icon to display in the title
 * @param onConfirm Callback when user confirms
 * @param onDismiss Callback when user dismisses
 * @param isDestructive Whether this is a destructive action (uses error colors)
 */
@Composable
fun SettingsConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    icon: ImageVector? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    IAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDestructive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = if (isDestructive) {
                        MaterialTheme.colorScheme.onError
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                )
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/**
 * Text input dialog for entering or editing text values.
 * 
 * @param title The dialog title
 * @param initialValue Initial text value
 * @param label Label for the text field
 * @param placeholder Placeholder text
 * @param confirmText Text for the confirm button (default: "Confirm")
 * @param dismissText Text for the dismiss button (default: "Cancel")
 * @param icon Optional icon to display in the title
 * @param validator Optional validation function that returns error message or null
 * @param onConfirm Callback when user confirms with the entered text
 * @param onDismiss Callback when user dismisses
 */
@Composable
fun SettingsTextInputDialog(
    title: String,
    initialValue: String = "",
    label: String,
    placeholder: String = "",
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    icon: ImageVector? = null,
    validator: ((String) -> String?)? = null,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    IAlertDialog(
        modifier = Modifier
            .heightIn(max = 400.dp, min = 250.dp)
            .widthIn(min = 280.dp),
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        // Clear error when user starts typing
                        if (errorMessage != null && it.isNotBlank()) {
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    isError = errorMessage != null,
                    supportingText = if (errorMessage != null) {
                        {
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val trimmedText = text.trim()
                            val validationError = validator?.invoke(trimmedText)
                            if (validationError != null) {
                                errorMessage = validationError
                            } else {
                                onConfirm(trimmedText)
                            }
                        }
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedText = text.trim()
                    val validationError = validator?.invoke(trimmedText)
                    if (validationError != null) {
                        errorMessage = validationError
                    } else {
                        onConfirm(trimmedText)
                    }
                },
                enabled = text.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/**
 * Single choice dialog for selecting one option from a list.
 * 
 * @param title The dialog title
 * @param options List of options to choose from
 * @param selectedIndex Currently selected option index
 * @param onOptionSelected Callback when an option is selected
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun <T> SettingsSingleChoiceDialog(
    title: String,
    options: List<T>,
    selectedIndex: Int,
    optionLabel: (T) -> String,
    onOptionSelected: (Int, T) -> Unit,
    onDismiss: () -> Unit
) {

    IAlertDialog(
        modifier = Modifier.heightIn(max = 400.dp, min = 200.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn {
                items(options.size, key = { it }) { index ->
                    val option = options[index]
                    val isSelected = index == selectedIndex
                    val label = optionLabel(option)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "$label. ${if (isSelected) "Selected" else "Not selected"}"
                                role = androidx.compose.ui.semantics.Role.RadioButton
                            }
                            .clickable {
                                onOptionSelected(index, option)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

/**
 * Multi-choice dialog for selecting multiple options from a list.
 * 
 * @param title The dialog title
 * @param options List of options to choose from
 * @param selectedIndices Currently selected option indices
 * @param optionLabel Function to get label for each option
 * @param onConfirm Callback when user confirms with selected indices
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun <T> SettingsMultiChoiceDialog(
    title: String,
    options: List<T>,
    selectedIndices: Set<Int>,
    optionLabel: (T) -> String,
    onConfirm: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedIndices) }
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    IAlertDialog(
        modifier = Modifier.heightIn(max = 400.dp, min = 200.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn {
                items(options.size, key = { it }) { index ->
                    val option = options[index]
                    val isSelected = index in currentSelection
                    val label = optionLabel(option)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "$label. ${if (isSelected) "Selected" else "Not selected"}"
                                role = androidx.compose.ui.semantics.Role.Checkbox
                            }
                            .clickable {
                                currentSelection = if (isSelected) {
                                    currentSelection - index
                                } else {
                                    currentSelection + index
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(currentSelection)
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.confirm),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.cancel),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
