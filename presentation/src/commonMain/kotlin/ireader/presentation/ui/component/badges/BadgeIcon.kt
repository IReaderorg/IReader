package ireader.presentation.ui.component.badges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import ireader.domain.models.remote.Badge
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Displays a badge icon.
 *
 * @param badge The badge to display
 * @param size The size of the badge icon (24.dp for reviews, 48.dp for profile, 32.dp for management)
 * @param modifier Optional modifier for the composable
 */
@Composable
fun BadgeIcon(
    badge: Badge,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = badge.imageUrl,
            contentDescription = "${badge.name} badge",
            modifier = Modifier.fillMaxSize(),
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier.size(size / 2),
                    strokeWidth = 2.dp
                )
            },
            error = {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Star,
                    contentDescription = localizeHelper.localize(Res.string.badge_placeholder),
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        )
    }
}

/**
 * Displays up to 3 featured badges in a horizontal row for user profiles.
 * 
 * @param badges List of badges to display (only first 3 will be shown)
 * @param modifier Optional modifier for the composable
 */
@Composable
fun ProfileBadgeDisplay(
    badges: List<Badge>,
    modifier: Modifier = Modifier
) {
    // Handle empty list gracefully - show nothing
    if (badges.isEmpty()) return
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display up to 3 badges
        badges.take(3).forEach { badge ->
            BadgeIcon(
                badge = badge,
                size = 48.dp
            )
        }
    }
}

/**
 * Displays a single primary badge next to username in reviews.
 * 
 * @param badge The badge to display (nullable - if null, nothing is rendered)
 * @param modifier Optional modifier for the composable
 */
@Composable
fun ReviewBadgeDisplay(
    badge: Badge?,
    modifier: Modifier = Modifier
) {
    // If badge is null, render nothing (no placeholder)
    badge?.let {
        BadgeIcon(
            badge = it,
            size = 24.dp,
            modifier = modifier
        )
    }
}
