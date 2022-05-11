package org.ireader.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.ireader.common_resources.UiText
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_api.source.model.Filter

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
        MidSizeTextComposable(text = UiText.DynamicString(filter.name))
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
