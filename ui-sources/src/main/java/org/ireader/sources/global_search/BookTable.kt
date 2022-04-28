package org.ireader.sources.global_search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.MidSizeTextComposable

@Composable
fun BookTable() {
    Column {
        Row(modifier = Modifier.fillMaxSize()) {
            MidSizeTextComposable(text = "Book Name")
            AppIconButton(imageVector = Icons.Default.ArrowForward,
                title = "Open Explore Screen",
                onClick = { /*TODO*/ })
        }
    }
}


@Preview
@Composable
fun BookTablePrev() {
    BookTable()
}