package org.ireader.bookDetails.components

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
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.components.reusable_composable.DropDownMenu
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.components.reusable_composable.TextField
import org.ireader.core_api.source.model.Command
import org.ireader.core_api.source.model.CommandList
import org.ireader.core_api.util.replace

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
                MidSizeTextComposable(text = "Reset", color = MaterialTheme.colors.primary)
            }
            Button(onClick = {
                onFetch()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(text = "Fetch", color = MaterialTheme.colors.onPrimary)
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
                        items = command.options
                    )
                }
                is Command.Chapter.Note -> {
                    MidSizeTextComposable(text = command.name)
                }
                else -> {}
            }
        }

    }
}