package ireader.presentation.ui.home.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.core.source.model.Filter
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable

@Composable
fun FilterBottomSheet(
    onApply: () -> Unit,
    onReset: () -> Unit,
    onUpdate: (List<Filter<*>>) -> Unit,
    filters: List<Filter<*>>,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            androidx.compose.material3.TextButton(onClick = {
                onReset()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(text = localize(MR.strings.reset), color = MaterialTheme.colorScheme.primary)
            }
            androidx.compose.material3.Button(onClick = {
                onApply()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(text = localize(MR.strings.apply), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        FilterGroupItem(
            name = "",
            filters = filters,
            onUpdate = {
                onUpdate(it)
            },
            isExpandable = false,
        )
    }
}

