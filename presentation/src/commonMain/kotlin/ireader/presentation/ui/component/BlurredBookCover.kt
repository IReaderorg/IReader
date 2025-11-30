package ireader.presentation.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * A book cover that can be blurred until tapped
 * Used for privacy when "Hide Content" is enabled
 */
@Composable
fun BlurredBookCover(
    imageUrl: String,
    contentDescription: String?,
    isBlurred: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var revealed by remember(imageUrl) { mutableStateOf(!isBlurred) }
    
    Box(
        modifier = modifier.clickable {
            if (isBlurred && !revealed) {
                revealed = true
            }
            onClick()
        }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isBlurred && !revealed) {
                        Modifier.blur(20.dp)
                    } else {
                        Modifier
                    }
                )
        )
        
        if (isBlurred && !revealed) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = localizeHelper.localize(Res.string.tap_to_reveal),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
