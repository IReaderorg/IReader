package ireader.presentation.ui.component.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Modern redesigned bottom navigation bar with enhanced visual design
 * Features:
 * - Smooth animations and transitions
 * - Material You design principles
 * - Elevated card-like appearance
 * - Gradient backgrounds for selected items
 * - Scale animations on selection
 * - Better spacing and visual hierarchy
 */
@Composable
fun ModernBottomNavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Modern navigation item with enhanced animations and visual design
 * Supports both single tap and double tap gestures
 */
@Composable
fun RowScope.ModernNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    onDoubleClick: (() -> Unit)? = null
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

    val animatedAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = localizeHelper.localize(Res.string.alpha)
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    // Fast double-tap detection without delaying single tap
    var lastClickTime by remember { mutableStateOf(0L) }
    val doubleTapTimeout = 300L
    
    val handleClick: () -> Unit = remember(onClick, onDoubleClick) {
        {
            val currentTime = currentTimeToLong()
            if (onDoubleClick != null && currentTime - lastClickTime < doubleTapTimeout) {
                // Double tap detected
                onDoubleClick()
                lastClickTime = 0L // Reset to prevent triple-tap
            } else {
                // Single tap - execute immediately
                onClick()
                lastClickTime = currentTime
            }
        }
    }

    Surface(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = handleClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
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
            Column(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon with background circle for selected state
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .then(
                            if (selected) {
                                Modifier
                                    .background(
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
                        modifier = Modifier.size(22.dp),
                        tint = if (selected) primaryColor else onSurface.copy(alpha = animatedAlpha)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Label with animation
                AnimatedVisibility(
                    visible = alwaysShowLabel || selected,
                    enter = fadeIn(animationSpec = tween(200)) + expandVertically(),
                    exit = fadeOut(animationSpec = tween(200)) + shrinkVertically()
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = if (selected) primaryColor else onSurface.copy(alpha = animatedAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact modern navigation item for tablets/navigation rail
 * Supports both single tap and double tap gestures
 */
@Composable
fun ModernNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    onDoubleClick: (() -> Unit)? = null
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
    
    // Fast double-tap detection without delaying single tap
    var lastClickTime by remember { mutableStateOf(0L) }
    val doubleTapTimeout = 300L
    
    val handleClick: () -> Unit = remember(onClick, onDoubleClick) {
        {
            val currentTime = currentTimeToLong()
            if (onDoubleClick != null && currentTime - lastClickTime < doubleTapTimeout) {
                // Double tap detected
                onDoubleClick()
                lastClickTime = 0L // Reset to prevent triple-tap
            } else {
                // Single tap - execute immediately
                onClick()
                lastClickTime = currentTime
            }
        }
    }

    Surface(
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = handleClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) primaryColor.copy(alpha = 0.15f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
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

            if (alwaysShowLabel || selected) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (selected) primaryColor else onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
