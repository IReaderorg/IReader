package ireader.presentation.ui.home.sources.extension

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.Catalog
import ireader.i18n.UiEvent
import ireader.i18n.asString
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.sources.extension.composables.ModernRemoteSourcesScreen
import ireader.presentation.ui.home.sources.extension.composables.ModernUserSourcesScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Modern redesigned Extension Screen with enhanced UI/UX
 * Features:
 * - Smooth tab animations
 * - Modern card-based design
 * - Better visual hierarchy
 * - Enhanced spacing and typography
 */
@ExperimentalMaterialApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModernExtensionScreen(
    modifier: Modifier = Modifier,
    vm: ExtensionViewModel,
    onClickCatalog: (Catalog) -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
    snackBarHostState: SnackbarHostState,
    onShowDetails: ((Catalog) -> Unit)? = null,
    onMigrateFromSource: ((Long) -> Unit)? = null,
    onNavigateToBrowseSettings: (() -> Unit)? = null,
    scaffoldPadding: PaddingValues
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current)
    
    LaunchedEffect(Unit) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(event.uiText.asString(localizeHelper))
                }
                else -> {}
            }
        }
    }

    val pages = remember {
        listOf(
            localizeHelper.localize(Res.string.sources),
            localizeHelper.localize(Res.string.extensions),
        )
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) { pages.size }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            vm.currentPagerPage = pagerState.currentPage
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
    ) {
        // Modern Tab Bar with gradient indicator
        ModernTabBar(
            pagerState = pagerState,
            pages = pages
        )

        // Content Pager
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> ModernUserSourcesScreen(
                    onClickCatalog = onClickCatalog,
                    onClickTogglePinned = onClickTogglePinned,
                    vm = vm,
                    onShowDetails = onShowDetails,
                    onMigrateFromSource = onMigrateFromSource,
                )
                1 -> ModernRemoteSourcesScreen(
                    vm = vm,
                    onClickInstall = onClickInstall,
                    onClickUninstall = onClickUninstall,
                    onCancelInstaller = onCancelInstaller,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModernTabBar(
    pagerState: PagerState,
    pages: List<String>,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scope = rememberCoroutineScope()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    // Calculate page offset for smooth animations
    val pageOffset = pagerState.currentPage + pagerState.currentPageOffsetFraction

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(surfaceColor)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        // Background pill that slides
        val pillOffset by animateFloatAsState(
            targetValue = pageOffset,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = localizeHelper.localize(Res.string.pill_offset)
        )
        
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            val tabWidth = maxWidth / pages.size
            
            // Animated background pill
            Box(
                modifier = Modifier
                    .width(tabWidth)
                    .fillMaxHeight()
                    .offset(x = tabWidth * pillOffset)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.15f),
                                primaryColor.copy(alpha = 0.25f),
                                primaryColor.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .drawBehind {
                        // Glow effect
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.3f),
                                    primaryColor.copy(alpha = 0.1f)
                                )
                            ),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
            )
            
            // Tab buttons
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                pages.forEachIndexed { index, title ->
                    val isSelected = pagerState.currentPage == index
                    val distance = abs(pageOffset - index)
                    val scale by animateFloatAsState(
                        targetValue = 1f - (distance * 0.1f).coerceIn(0f, 0.15f),
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "scale_$index"
                    )
                    
                    CustomTab(
                        text = title,
                        isSelected = isSelected,
                        scale = scale,
                        onClick = {
                            scope.launch { 
                                pagerState.animateScrollToPage(
                                    index,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomTab(
    text: String,
    isSelected: Boolean,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 250),
        label = localizeHelper.localize(Res.string.tab_content)
    )
    
    val fontWeight by animateIntAsState(
        targetValue = if (isSelected) 700 else 500,
        animationSpec = tween(durationMillis = 250),
        label = localizeHelper.localize(Res.string.font_weight_1)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight(fontWeight),
                fontSize = if (isSelected) 16.sp else 15.sp
            ),
            color = contentColor
        )
    }
}
