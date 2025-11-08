package ireader.presentation.ui.component.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import ireader.i18n.Images.eternityLight
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.ui.Colour.Transparent

@Composable
fun LogoHeader() {
    // Animation state for entrance effect
    var isVisible by remember { mutableStateOf(false) }
    
    // Trigger animation on composition
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    // Animate scale and alpha for smooth entrance
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        )
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
    )
    
    Column {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.Transparent)
                .fillMaxWidth(),
            contentColor = AppColors.current.bars,
            color = AppColors.current.bars
        ) {
            // Center the logo with Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp), // Increased vertical spacing
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = eternityLight(),
                    contentDescription = "IReader Logo",
                    tint = AppColors.current.onBars,
                    modifier = Modifier
                        .size(140.dp) // Increased from 100.dp to 140.dp
                        .scale(scale)
                        .alpha(alpha),
                )
            }
        }

        Divider()
    }
}
