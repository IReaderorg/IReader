package org.ireader.components.reusable_composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.UiText

@Composable
fun TextField(
    filter: org.ireader.core_api.source.model.Filter.Text,
    onUpdate: (String) -> Unit,
) {
    var state by remember {
        mutableStateOf(filter.value)
    }
    if (filter.value.isBlank()) {
        state = ""
    }
    Box(
        modifier = Modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        OutlinedTextField(
            value = state, onValueChange = {
                onUpdate(it)
                state = it
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        if (filter.value.isBlank()) {
            MidSizeTextComposable(
                text = UiText.DynamicString(filter.name),
                color = MaterialTheme.colors.onBackground.copy(alpha = .4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun TextField(
    command: org.ireader.core_api.source.model.Command.Chapter.Text,
    onUpdate: (String) -> Unit,
) {
    var state by remember {
        mutableStateOf(command.value)
    }
    if (command.value.isBlank()) {
        state = ""
    }
    Box(
        modifier = Modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        OutlinedTextField(
            value = state, onValueChange = {
                onUpdate(it)
                state = it
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        if (command.value.isBlank()) {
            MidSizeTextComposable(
                text =   UiText.DynamicString(command.name),
                color = MaterialTheme.colors.onBackground.copy(alpha = .4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}