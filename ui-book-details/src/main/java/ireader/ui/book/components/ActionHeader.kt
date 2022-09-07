package ireader.ui.book.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.core.api.source.HttpSource
import ireader.core.api.source.Source
import ireader.common.resources.R

@Composable
fun ActionHeader(
    modifier: Modifier = Modifier,
    favorite:Boolean,
    source: Source?,
    onWebView: () -> Unit,
    onFavorite:() -> Unit
) {
    Row(modifier = modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp))  {
        val defaultActionButtonColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)
        ActionButton(
            title = if (favorite) {
                stringResource(R.string.in_library)
            } else {
                stringResource(R.string.add_to_library)
            },
            icon = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            color = if (favorite) MaterialTheme.colorScheme.primary else defaultActionButtonColor,
            onClick = onFavorite,
            onLongClick = {},
        )
        if (source is HttpSource) {
            ActionButton(
                title = stringResource(R.string.webView),
                icon = Icons.Default.Public,
                color = defaultActionButtonColor,
                onClick = onWebView,
            )
        }

    }
}


@Composable
private fun RowScope.ActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                color = color,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}



