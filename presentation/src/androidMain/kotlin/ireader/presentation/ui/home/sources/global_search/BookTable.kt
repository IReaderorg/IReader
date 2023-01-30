package ireader.presentation.ui.home.sources.global_search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ireader.presentation.R
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable


@Composable
fun BookTable() {
    Column {
        Row(modifier = Modifier.fillMaxSize()) {
            MidSizeTextComposable(text = stringResource(R.string.book_name),)
            AppIconButton(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = stringResource(R.string.open_explore),
            )
        }
    }
}

@Preview
@Composable
fun BookTablePrev() {
    BookTable()
}
