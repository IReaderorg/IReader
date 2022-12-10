package ireader.presentation.ui.component.reusable_composable

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ireader.presentation.R

@Composable
fun BuildDropDownMenu(
    items: List<DropDownMenuItem>,
    onExpand: () -> Boolean = {false}
) {
    val (state, setState) = remember {
        mutableStateOf(false)
    }
    DisposableEffect(key1 = onExpand ) {
        setState(onExpand())
        onDispose {  }
    }

    Box {
        IconButton(onClick = { setState(true) }) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(R.string.export_book_as_epub),
            )
        }
        DropdownMenu(
            modifier = Modifier,
            expanded = state,
            onDismissRequest = {
                setState(false)
            },
        ) {
            items.forEachIndexed { _, item ->
                DropdownMenuItem(
                    onClick = {
                        item.onItem()
                        setState(false)
                    },
                    text = { MidSizeTextComposable(text = item.text) },
                )
            }
        }
    }
}

data class DropDownMenuItem(
    val text: String,
    val onItem: () -> Unit,
)
