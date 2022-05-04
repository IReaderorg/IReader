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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.components.reusable_composable.DropDownMenu
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_api.source.model.Command
import org.ireader.core_api.source.model.CommandList
import org.ireader.core_api.source.model.Filter
import org.ireader.explore.TextField

@Composable
fun ChapterCommandBottomSheet(
    onFetch: () -> Unit,
    onReset: () -> Unit,
    onUpdate: (List<Filter<*>>) -> Unit,
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
        commandList.forEach { command ->
            Spacer(modifier = Modifier.height(8.dp))
            when (command) {
                is Command.Chapter.Text -> {
                    TextField(initialValue = command.initialValue, hint = command.name, onUpdate = {})
                }
                is Command.Chapter.Select -> {
                    DropDownMenu(
                        text = command.name,
                        onSelected = {},
                        items = command.options
                    )
                }
                is Command.Chapter.Numeric -> {
                    TextField(initialValue = command.initialValue.toString(), hint = command.name, onUpdate = {})
                }
                is Command.Chapter.Note -> {
                    MidSizeTextComposable(text = command.name)
                }
            }
        }

    }
}