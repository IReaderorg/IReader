package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Book
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.ChipPreference
import ireader.presentation.ui.component.components.IAlertDialog
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Dialog for editing book information.
 * 
 * Custom cover is stored in `customCover` field which is preserved when updating
 * book details from remote source. This ensures user-set covers are not overwritten.
 */
@Composable
fun EditInfoAlertDialog(onStateChange: (Boolean) -> Unit, book: Book, onConfirm: (Book) -> Unit) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var state by remember {
        mutableStateOf(false)
    }
    var title by remember {
        mutableStateOf(book.title)
    }
    var author by remember {
        mutableStateOf(book.author)
    }
    var description by remember {
        mutableStateOf(book.description)
    }
    // Use customCover if set, otherwise fall back to cover
    // This allows users to see and edit their custom cover
    var customCover by remember {
        mutableStateOf(
            if (book.customCover.isNotBlank() && book.customCover != book.cover) {
                book.customCover
            } else {
                book.cover
            }
        )
    }
    var status by remember {
        mutableStateOf(book.status)
    }
    
    // Track if user has modified the cover
    val hasCustomCover = customCover.isNotBlank() && customCover != book.cover
    
    // Validation state
    val isTitleValid = title.isNotBlank()
    val isCoverUrlValid = customCover.isBlank() || customCover.startsWith("http://") || customCover.startsWith("https://") || customCover.startsWith("file://")
    val canSave = isTitleValid && isCoverUrlValid
    
    IAlertDialog(
        onDismissRequest = {
            onStateChange(state)
            state = false
        },
        confirmButton = {
            Row {
                TextButton(onClick = {
                    onStateChange(state)
                    state = false
                }) {
                    MidSizeTextComposable(text = localizeHelper.localize(Res.string.cancel))
                }
                TextButton(
                    onClick = {
                        if (canSave) {
                            onStateChange(state)
                            state = false
                            // Update customCover field - this is preserved on remote updates
                            // The cover field remains unchanged (source cover)
                            // Update lastUpdate if customCover changed to invalidate image cache
                            val coverChanged = customCover.trim() != book.customCover && 
                                customCover.trim() != book.cover
                            onConfirm(
                                book.copy(
                                    title = title.trim(),
                                    author = author.trim(),
                                    description = description.trim(),
                                    customCover = customCover.trim(),
                                    status = status,
                                    lastUpdate = if (coverChanged) currentTimeToLong() else book.lastUpdate
                                )
                            )
                        }
                    },
                    enabled = canSave
                ) {
                    MidSizeTextComposable(text = localizeHelper.localize(Res.string.confirm))
                }
            }
        },
        title = {
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.edit_info))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MidSizeTextComposable(text = localizeHelper.localize(Res.string.status))
                    Spacer(modifier = Modifier.width(8.dp))
                    ChipPreference(
                        preference = book.allStatus(),
                        selected = status.toInt(),
                        title = localizeHelper.localize(Res.string.status),
                        onValueChange = {
                            status = it.toLong()
                        }
                    )
                }
                
                SimpleTextField(
                    query = title,
                    onValueChange = { title = it },
                    onConfirm = {},
                    hint = localizeHelper.localize(Res.string.title),
                    isError = !isTitleValid
                )
                if (!isTitleValid) {
                    Text(
                        text = localizeHelper.localize(Res.string.title_cannot_be_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                SimpleTextField(
                    query = author,
                    onValueChange = { author = it },
                    onConfirm = {},
                    hint = localizeHelper.localize(Res.string.author)
                )
                
                SimpleTextField(
                    query = description,
                    onValueChange = { description = it },
                    onConfirm = {},
                    hint = localizeHelper.localize(Res.string.description),
                    maxLines = 5
                )
                
                // Custom cover field with reset option
                SimpleTextField(
                    query = customCover,
                    onValueChange = { customCover = it },
                    onConfirm = {},
                    hint = localizeHelper.localize(Res.string.custom_cover),
                    isError = !isCoverUrlValid
                )
                if (!isCoverUrlValid) {
                    Text(
                        text = localizeHelper.localize(Res.string.cover_url_must_start_with_http_https_or_file),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                // Show reset button if custom cover is set
                if (hasCustomCover) {
                    TextButton(
                        onClick = { customCover = book.cover },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.reset_cover),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTextField(
    modifier: Modifier = Modifier,
    query: String,
    onValueChange: (value: String) -> Unit,
    onConfirm: () -> Unit,
    hint: String,
    keyboardAction: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions(onDone = { onConfirm() }),
    maxLines: Int = 1,
    isError: Boolean = false
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        value = query,
        onValueChange = onValueChange,
        maxLines = maxLines,
        keyboardOptions = keyboardAction,
        keyboardActions = keyboardActions,
        singleLine = maxLines == 1,
        isError = isError,
        textStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        placeholder = {
            Text(
                modifier = Modifier,
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (maxLines == 1) 1 else 2,
                overflow = TextOverflow.Ellipsis
            )
        },
    )
}
