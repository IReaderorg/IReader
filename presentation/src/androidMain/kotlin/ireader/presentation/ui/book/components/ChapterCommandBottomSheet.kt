package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.reusable_composable.DropDownMenu
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TextField
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.core.util.replace
import ireader.presentation.R

@Composable
fun ChapterCommandBottomSheet(
    onFetch: () -> Unit,
    onReset: () -> Unit,
    onUpdate: (List<Command<*>>) -> Unit,
    commandList: CommandList,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = {
                onReset()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(text = stringResource(R.string.reset), color = MaterialTheme.colorScheme.primary)
            }
            Button(onClick = {
                onFetch()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(text = stringResource(R.string.fetch), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        commandList.forEachIndexed { index, command ->
            Spacer(modifier = Modifier.height(8.dp))
            when (command) {
                is Command.Chapter.Text -> {
                    TextField(
                        command = command,
                        onUpdate = {
                            onUpdate(
                                commandList.replace(
                                    index,
                                    command.apply {
                                        this.value = it
                                    }
                                )
                            )
                        },
                    )
                }
                is Command.Chapter.Select -> {
                    var state by remember {
                        mutableStateOf(command.initialValue)
                    }
                    LaunchedEffect(key1 = command.value) {
                        state = command.value
                    }
                    DropDownMenu(
                        text = command.name,
                        onSelected = { value ->
                            onUpdate(
                                commandList.replace(
                                    index,
                                    command.apply {
                                        this.value = value
                                    }
                                )
                            )
                            state = value
                        },
                        currentValue = command.options[state],
                        items = command.options.map { it }.toTypedArray()
                    )
                }
                is Command.Chapter.Note -> {
                    Text(
                        text = command.name,
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.onSurface.copy(.8f),
                        textAlign = TextAlign.Justify,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                else -> {}
            }
        }
    }
}
