package ireader.presentation.ui.component.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Material 3 NavigationRail with modern design
 * Features:
 * - Vertical navigation for tablets and landscape
 * - Smooth animations and transitions
 * - Material You design principles
 * - Better visual hierarchy
 * - Compact and space-efficient
 */
@Composable
fun Material3NavigationRail(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .shadow(
                elevation = 3.dp,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            header?.invoke(this)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

/**
 * Material 3 NavigationRail item with enhanced design
 */
@Composable
fun Material3NavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = false
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = localizeHelper.localize(Res.string.scale)
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier
            .width(64.dp)
            .scale(animatedScale),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            primaryColor.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        }
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .then(
                        if (selected) {
                            Modifier.background(
                                color = primaryColor.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = if (selected) primaryColor else onSurface.copy(alpha = 0.6f)
                )
            }

            // Label
            if (alwaysShowLabel || selected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 11.sp
                    ),
                    color = if (selected) primaryColor else onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

/**
 * Compact NavigationRail item (icon only by default)
 */
@Composable
fun CompactNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = localizeHelper.localize(Res.string.scale)
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .width(64.dp)
            .scale(animatedScale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            color = if (selected) {
                primaryColor.copy(alpha = 0.15f)
            } else {
                Color.Transparent
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (selected) {
                            Modifier.background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.15f),
                                        primaryColor.copy(alpha = 0.08f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = if (selected) primaryColor else onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Small indicator dot
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .background(primaryColor, CircleShape)
            )
        }
    }
}
