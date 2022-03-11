package org.ireader.presentation.feature_explore.presentation.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Checkbox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import tachiyomi.source.model.Filter

@Composable
fun FilterCheckItem(
    filter: Filter.Check,
    onCheck: (Boolean) -> Unit,
) {
    var state by remember {
        mutableStateOf(filter.initialValue ?: false)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MidSizeTextComposable(text = filter.name)
        Checkbox(checked = state,
            onCheckedChange = { value ->
                onCheck(value)
                state = value
            })
    }
}