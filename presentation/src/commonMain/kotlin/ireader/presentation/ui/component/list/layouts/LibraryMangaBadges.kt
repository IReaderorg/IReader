package ireader.presentation.ui.component.list.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LibraryBadges(
    unread: Int?,
    downloaded: Int?,
    modifier: Modifier = Modifier,
    isLocal: Boolean = false,
    sourceId: Long? = null,
    showLanguage: Boolean = false,
    isPinned: Boolean = false,
) {
    // Get language from catalog if needed
    val language = if (showLanguage && sourceId != null) {
        val catalogStore = org.koin.compose.koinInject<ireader.domain.catalogs.CatalogStore>()
        catalogStore.get(sourceId)?.source?.lang
    } else null

    if (unread == null && downloaded == null && !isLocal && language == null && !isPinned) return

    Column(modifier = modifier) {
        // Pin indicator at the top
        if (isPinned) {
            Row {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
        
        // Badges row
        Row {
            if (unread != null && unread > 0) {
                BookCoverBadge(
                    text = "$unread",
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            if (downloaded != null && downloaded > 0) {
                BookCoverBadge(
                    text = "$downloaded",
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    textColor = MaterialTheme.colorScheme.onTertiary
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            if (isLocal) {
                BookCoverBadge(
                    text = "LOCAL",
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            if (language != null && language.isNotEmpty()) {
                BookCoverBadge(
                    text = language.take(2).uppercase(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BookCoverBadge(
    text: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = textColor
    )
}

@Composable
private fun BadgeItem(
    text: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = textColor
    )
}
