package ireader.ui.book.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.common.models.entities.Book
import ireader.presentation.R
import ireader.ui.component.components.component.ChipPreference
import ireader.ui.component.reusable_composable.MidSizeTextComposable

@Composable
fun EditInfoAlertDialog(onStateChange: (Boolean) -> Unit, book: Book,onConfirm: (Book) -> Unit) {
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
    AlertDialog(
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
                    MidSizeTextComposable(text = stringResource(id = R.string.cancel))
                }
                TextButton(onClick = {
                    onStateChange(state)
                    state = false
                    onConfirm(book.copy(description = description.ifBlank { book.description }, title = title.ifBlank { book.title }, cover = cover.ifBlank { book.cover }, author = author.ifBlank { book.author }, status = status))
                }) {
                    MidSizeTextComposable(text = stringResource(id = R.string.confirm))
                }

            }
        },

        title = {
            MidSizeTextComposable(text = "Edit Info")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically,) {
                    MidSizeTextComposable(text = "Status")
                    Spacer(modifier = Modifier.width(8.dp))
                    ChipPreference(preference = book.allStatus(), selected = status.toInt(), title = stringResource(
                        id = R.string.status
                    ), onValueChange = {
                        status = it.toLong()
                    } )
                }
                SimpleTextField(
                    query = title,
                    onValueChange = { title = it },
                    onConfirm = {},
                    hint = stringResource(id = R.string.title) + ": " + book.title
                )
                SimpleTextField(
                    query = author,
                    onValueChange = { author = it },
                    onConfirm = {},
                    hint = stringResource(id = R.string.author) + ": " + book.author
                )
                SimpleTextField(
                    query = description,
                    onValueChange = { description = it },
                    onConfirm = {},
                    hint = stringResource(id = R.string.description) + ": " + book.description
                )
                SimpleTextField(
                    query = cover,
                    onValueChange = { cover = it },
                    onConfirm = {},
                    hint = stringResource(id = R.string.cover) + ": " + book.cover
                )
//                SimpleTextField(
//                    query = tags,
//                    onValueChange = { tags = it },
//                    onConfirm = {
//                                editedTags = editedTags + tags
//                        tags = ""
//                    },
//                    hint = stringResource(id = R.string.tags)
//                )
            }
        }
    )
}

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
