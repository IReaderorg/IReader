package ireader.presentation.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.bookmark
import ireader.i18n.resources.expand_menu
import ireader.i18n.resources.find_in_chapter
import ireader.i18n.resources.refresh
import ireader.i18n.resources.report_broken_chapter
import ireader.i18n.resources.webView
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreenTopBar(
    modifier: Modifier = Modifier,
    isReaderModeEnable: Boolean,
    vm: ReaderScreenViewModel,
    state: ReaderScreenViewModel, // Using ViewModel directly for state access
    chapter: Chapter?,
    onRefresh: () -> Unit,
    onWebView: () -> Unit,
    onBookMark: () -> Unit,
    onPopBackStack: () -> Unit,
    onChapterArt: () -> Unit = {},
    isLoaded: Boolean = false,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }

    // Main top bar with instant animation (shown when reader mode is disabled)
    AnimatedVisibility(
        visible = !isReaderModeEnable && isLoaded,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(
                durationMillis = 100,
                easing = androidx.compose.animation.core.LinearEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(
                durationMillis = 50,
                easing = androidx.compose.animation.core.LinearEasing
            )
        ),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                TopAppBarBackButton(
                    onClick = { onPopBackStack() }
                )

                // Title
                Text(
                    text = chapter?.name ?: "",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                // Actions
                if (chapter != null) {
                    AppIconButton(
                        imageVector = if (vm.expandTopMenu) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
                        contentDescription = localize(Res.string.expand_menu),
                        onClick = {
                            vm.expandTopMenu = !vm.expandTopMenu
                        }
                    )
                    
                    if (vm.expandTopMenu) {
                        AppIconButton(
                            imageVector = if (chapter.bookmark) Icons.Filled.Bookmark else Icons.Default.Bookmark,
                            contentDescription = localize(Res.string.bookmark),
                            tint = if (chapter.bookmark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            onClick = { onBookMark() }
                        )
                        AppIconButton(
                            imageVector = Icons.Default.Search,
                            contentDescription = localizeHelper.localize(Res.string.find_in_chapter),
                            onClick = { vm.toggleFindInChapter() }
                        )
                        AppIconButton(
                            imageVector = Icons.Default.Report,
                            contentDescription = localizeHelper.localize(Res.string.report_broken_chapter),
                            onClick = { vm.toggleReportDialog() }
                        )
                        AppIconButton(
                            imageVector = Icons.Default.Brush,
                            contentDescription = "Generate Chapter Art",
                            onClick = { onChapterArt() }
                        )
                        if (!vm.webViewIntegration.value) {
                            AppIconButton(
                                imageVector = Icons.Default.Public,
                                contentDescription = localize(Res.string.webView),
                                onClick = { onWebView() }
                            )
                        }
                    }
                    
                    AppIconButton(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = localize(Res.string.refresh),
                        onClick = { onRefresh() }
                    )
                }
            }
        }
    }

    // Minimal top bar when not loaded (always visible)
    if (!isLoaded) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = vm.backgroundColor.value.toComposeColor(),
            contentColor = vm.textColor.value.toComposeColor(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopAppBarBackButton(
                    onClick = { onPopBackStack() },
                    tint = vm.textColor.value.toComposeColor()
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (chapter != null) {
                    AppIconButton(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = localize(Res.string.refresh),
                        onClick = { onRefresh() },
                        tint = vm.textColor.value.toComposeColor()
                    )
                    AppIconButton(
                        imageVector = Icons.Default.Public,
                        contentDescription = localize(Res.string.webView),
                        onClick = { onWebView() },
                        tint = vm.textColor.value.toComposeColor()
                    )
                }
            }
        }
    }
}
