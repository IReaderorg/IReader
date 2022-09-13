package ireader.ui.component.list.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun LibraryBadges(
    unread: Int?,
    downloaded: Int?,
    modifier: Modifier = Modifier,
) {
    if (unread == null && downloaded == null) return

    Row(modifier = modifier.clip(androidx.compose.material3.MaterialTheme.shapes.medium)) {
        if (unread != null && unread > 0) {
            Text(
                text = unread.toString(),
                modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.primary).then(BadgesInnerPadding),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
            )
        }
        if (downloaded != null && downloaded > 0) {
            Text(
                text = downloaded.toString(),
                modifier = Modifier.background(MaterialTheme.colorScheme.tertiary).then(BadgesInnerPadding),
                style = MaterialTheme.typography.labelSmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

private val BadgesInnerPadding = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
