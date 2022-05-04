package org.ireader.explore

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
import org.ireader.components.reusable_composable.MidSizeTextComposable

@Composable
fun TextField(
    initialValue: String,
    hint:String,
    onUpdate: (String) -> Unit,
) {
    var state by remember {
        mutableStateOf(initialValue)
    }
    if (initialValue.isBlank()) {
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
        if (initialValue.isBlank()) {
            MidSizeTextComposable(
                text = hint,
                color = MaterialTheme.colors.onBackground.copy(alpha = .4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
