package org.ireader.components.reusable_composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
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
                text = filter.name,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                text = command.name,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
