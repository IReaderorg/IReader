package ireader.presentation.ui.settings.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.domain.models.theme.Theme
import ireader.presentation.core.toComposeColor

@Composable
fun ThemePreviewCard(
    theme: Theme,
    themeName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .width(120.dp)
                .height(220.dp)
                .scale(scale)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation
            ),
            border = if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else null
        ) {
            // Phone mockup container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black) // Phone bezel
                    .padding(3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(13.dp))
                        .background(theme.materialColors.background.toComposeColor())
                ) {
                    // Notch
                    PhoneNotch(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black
                    )

                    // Status bar
                    PhoneStatusBar(
                        backgroundColor = theme.materialColors.background.toComposeColor(),
                        contentColor = theme.materialColors.onBackground.toComposeColor()
                    )

                    // App preview
                    MiniAppPreview(
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )

                    // Navigation bar
                    PhoneNavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                        color = theme.materialColors.background.toComposeColor()
                    )
                }
            }
        }

        // Theme name label
        Text(
            text = themeName,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 2
        )
    }
}
