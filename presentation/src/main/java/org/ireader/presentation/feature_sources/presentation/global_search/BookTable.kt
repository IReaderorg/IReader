package org.ireader.presentation.feature_sources.presentation.global_search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton

@Composable
fun BookTable() {
    Column {
        Row(modifier = Modifier.fillMaxSize()) {
            MidSizeTextComposable(text = "Book Name")
            TopAppBarActionButton(imageVector = Icons.Default.ArrowForward,
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