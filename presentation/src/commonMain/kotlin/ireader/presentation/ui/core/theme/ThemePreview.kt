package ireader.presentation.ui.core.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ireader.domain.models.theme.ExtraColors
import ireader.domain.models.theme.Theme
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toComposeColorScheme
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Preview composable for theme options
 * Requirements: 3.3
 */
@Composable
fun ThemePreview(
    theme: Theme,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AppColors(
        materialColors = theme.materialColors.toComposeColorScheme(),
        extraColors = theme.extraColors,
        typography = Typography(),
        shape = Shapes()
    ) {
        Surface(
            modifier = modifier,
            color = theme.materialColors.background.toComposeColor()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top bar preview
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    color = theme.extraColors.bars.toComposeColor(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.titleLarge,
                            color = theme.extraColors.onBars.toComposeColor()
                        )
                    }
                }
                
                // Content preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Primary button
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.materialColors.primary.toComposeColor(),
                            contentColor = theme.materialColors.onPrimary.toComposeColor()
                        )
                    ) {
                        Text(localizeHelper.localize(Res.string.primary_button))
                    }
                    
                    // Secondary button
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = theme.materialColors.secondary.toComposeColor()
                        )
                    ) {
                        Text(localizeHelper.localize(Res.string.secondary_button))
                    }
                    
                    // Card preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = theme.materialColors.surface.toComposeColor(),
                            contentColor = theme.materialColors.onSurface.toComposeColor()
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Card Title",
                                style = MaterialTheme.typography.titleMedium,
                                color = theme.materialColors.onSurface.toComposeColor()
                            )
                            Text(
                                text = "This is a preview of how text will look in cards with this theme.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.materialColors.onSurfaceVariant.toComposeColor()
                            )
                        }
                    }
                    
                    // Surface variant preview
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = theme.materialColors.surfaceVariant.toComposeColor(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = theme.materialColors.primary.toComposeColor()
                            )
                            Text(
                                text = "Surface Variant",
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.materialColors.onSurfaceVariant.toComposeColor()
                            )
                        }
                    }
                }
                
                // Bottom navigation preview
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    color = theme.materialColors.surfaceVariant.toComposeColor(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = theme.materialColors.primary.toComposeColor()
                        )
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = theme.materialColors.onSurfaceVariant.toComposeColor()
                        )
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = theme.materialColors.onSurfaceVariant.toComposeColor()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Minimal theme preview for grid display
 * Requirements: 3.3
 */
@Composable
fun ThemePreviewMini(
    theme: Theme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = theme.materialColors.background.toComposeColor(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(theme.extraColors.bars.toComposeColor())
            )
            
            // Content area with color samples
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Primary color
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            theme.materialColors.primary.toComposeColor(),
                            RoundedCornerShape(4.dp)
                        )
                )
                
                // Secondary color
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            theme.materialColors.secondary.toComposeColor(),
                            RoundedCornerShape(4.dp)
                        )
                )
                
                // Tertiary color
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            theme.materialColors.tertiary.toComposeColor(),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}
