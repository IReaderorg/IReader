package ireader.presentation.ui.home.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.core.source.model.Filter
import ireader.i18n.localize

import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable

@Composable
fun FilterBottomSheet(
    modifier: Modifier,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onUpdate: (List<Filter<*>>) -> Unit,
    filters: List<Filter<*>>,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            androidx.compose.material3.TextButton(onClick = {
                onReset()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(
                    text = localize { xml -> xml.reset },
                    color = MaterialTheme.colorScheme.primary
                )
            }
            androidx.compose.material3.Button(onClick = {
                onApply()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(
                    text = localize { xml -> xml.apply },
                    color = MaterialTheme.colorScheme.onPrimary
                )
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

