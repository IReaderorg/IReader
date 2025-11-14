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
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.entities.Book
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.ChipPreference
import ireader.presentation.ui.component.components.IAlertDialog
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@Composable
fun EditInfoAlertDialog(onStateChange: (Boolean) -> Unit, book: Book, onConfirm: (Book) -> Unit) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
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
    var cover by remember {
        mutableStateOf(book.cover)
    }
    var status by remember {
        mutableStateOf(book.status)
    }
    
    // Validation state
    val isTitleValid = title.isNotBlank()
    val isCoverUrlValid = cover.isBlank() || cover.startsWith("http://") || cover.startsWith("https://") || cover.startsWith("file://")
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
                            onConfirm(
                                book.copy(
                                    title = title.trim(),
                                    author = author.trim(),
                                    description = description.trim(),
                                    cover = cover.trim(),
                                    status = status
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
            MidSizeTextComposable(text = "Edit Info")
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
                        text = "Title cannot be empty",
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
                
                SimpleTextField(
                    query = cover,
                    onValueChange = { cover = it },
                    onConfirm = {},
                    hint = "Cover URL",
                    isError = !isCoverUrlValid
                )
                if (!isCoverUrlValid) {
                    Text(
                        text = "Cover URL must start with http://, https://, or file://",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
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
