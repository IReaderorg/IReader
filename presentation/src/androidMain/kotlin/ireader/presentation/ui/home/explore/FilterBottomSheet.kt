package ireader.presentation.ui.home.explore

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ireader.core.source.CatalogSource
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.core.source.TestSource
import ireader.core.source.model.Filter
import ireader.presentation.R

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
                MidSizeTextComposable(text = stringResource(R.string.reset), color = MaterialTheme.colorScheme.primary)
            }
            androidx.compose.material3.Button(onClick = {
                onApply()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(text = stringResource(R.string.apply), color = MaterialTheme.colorScheme.onPrimary)
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FilterBottomSheetPrev() {
    FilterBottomSheet(
        {},
        {},
        { },
        (TestSource() as CatalogSource).getFilters()
    )
}
