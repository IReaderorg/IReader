package org.ireader.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.ireader.core_api.source.model.Filter
import org.ireader.core_ui.ui_components.reusable_composable.MidSizeTextComposable

@Composable
fun FilterCheckItem(
    filter: Filter.Check,
    onCheck: (Boolean?) -> Unit,
) {
    var state by remember {
        mutableStateOf(filter.initialValue)
    }
    LaunchedEffect(key1 = filter.value) {
        state = filter.value
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MidSizeTextComposable(text = filter.name)
        UnCheckedCheckBox(
            isChecked = state,
            filter.allowsExclusion,
            onChecked = {
                state = it
                onCheck(it)
            }
        )
    }
}