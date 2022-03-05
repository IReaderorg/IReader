package org.ireader.presentation.feature_explore.presentation.browse

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton

@Composable
fun FilterItem() {
    Box(modifier = Modifier) {
        MidSizeTextComposable(text = "Sort By:")
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "",
                tint = MaterialTheme.colors.primary,

                )
        }
        TopAppBarActionButton(
            imageVector = Icons.Default.ArrowDownward,
            title = "",
            onClick = { })
    }
}


@Preview
@Composable
fun FilterItemPrev() {
    FilterItem()
}