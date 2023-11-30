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
        mutableStateOf("")
    }
    var author by remember {
        mutableStateOf("")
    }
    var description by remember {
        mutableStateOf("")
    }
    var cover by remember {
        mutableStateOf("")
    }
    var tags by remember {
        mutableStateOf("")
    }
    var editedTags by remember {
        mutableStateOf(book.genres)
    }
    var status by remember {
        mutableStateOf(book.status)
    }
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
                    MidSizeTextComposable(text = localizeHelper.localize() { xml ->
                        xml.cancel
                    })
                }
                TextButton(onClick = {
                    onStateChange(state)
                    state = false
                    onConfirm(
                        book.copy(
                            description = description.ifBlank { book.description },
                            title = title.ifBlank { book.title },
                            cover = cover.ifBlank { book.cover },
                            author = author.ifBlank { book.author },
                            status = status
                        )
                    )
                }) {
                    MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.confirm })
                }

            }
        },

        title = {
            MidSizeTextComposable(text = "Edit Info")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MidSizeTextComposable(text = "Status")
                    Spacer(modifier = Modifier.width(8.dp))
                    ChipPreference(preference = book.allStatus(),
                        selected = status.toInt(),
                        title = localizeHelper.localize {
                            it.status
                        },
                        onValueChange = {
                            status = it.toLong()
                        })
                }
                SimpleTextField(
                    query = title,
                    onValueChange = { title = it },
                    onConfirm = {},
                    hint = localizeHelper.localize { xml -> xml.title } + ": " + book.title
                )
                SimpleTextField(
                    query = author,
                    onValueChange = { author = it },
                    onConfirm = {},
                    hint = localizeHelper.localize { xml -> xml.author } + ": " + book.author
                )
                SimpleTextField(
                    query = description,
                    onValueChange = { description = it },
                    onConfirm = {},
                    hint = localizeHelper.localize { xml -> xml.description } + ": " + book.description
                )
                SimpleTextField(
                    query = cover,
                    onValueChange = { cover = it },
                    onConfirm = {},
                    hint = localizeHelper.localize { xml -> xml.cover } + ": " + book.cover
                )
//                SimpleTextField(
//                    query = tags,
//                    onValueChange = { tags = it },
//                    onConfirm = {
//                                editedTags = editedTags + tags
//                        tags = ""
//                    },
//                    hint = localizeHelper.localize{ xml-> xml.tags)
//                )
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
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        value = query,
        onValueChange = onValueChange,
        maxLines = 1,
        keyboardOptions = keyboardAction,
        keyboardActions = keyboardActions,
        singleLine = true,
        textStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        placeholder = {
            Text(
                modifier = Modifier,
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
    )
}
