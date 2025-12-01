package ireader.presentation.ui.component.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.Images.eternityLight
import ireader.i18n.resources.Res
import ireader.i18n.resources.ireader_logo
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.Colour.Transparent

@Composable
fun LogoHeader() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Animation state for entrance effect
    var isVisible by remember { mutableStateOf(false) }

    Column {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.Transparent)
                .fillMaxWidth(),
            contentColor = AppColors.current.bars.toComposeColor(),
            color = AppColors.current.bars.toComposeColor()
        ) {
            // Center the logo with Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 50.dp), // Increased vertical spacing
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = eternityLight(),
                    contentDescription = localizeHelper.localize(Res.string.ireader_logo),
                    tint = AppColors.current.onBars.toComposeColor(),
                    modifier = Modifier
                        .size(80.dp) // Increased from 100.dp to 140.dp
                )
            }
        }

        Divider()
    }
}
