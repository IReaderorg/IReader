package ireader.presentation.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.reader.components.TTSButton
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Desktop-specific reader top bar with TTS button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopReaderScreenTopBar(
    vm: ReaderScreenViewModel,
    chapter: Chapter?,
    onPopBackStack: () -> Unit,
    onBookMark: () -> Unit,
    onWebView: () -> Unit,
    onRefresh: () -> Unit,
    onToggleTTSControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val isLoaded = chapter != null && !vm.isLoading

    AnimatedVisibility(
        visible = !vm.isReaderModeEnable,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(250)),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(250))
    ) {
        Toolbar(
            modifier = modifier,
            title = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = chapter?.name ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                }
            },
            backgroundColor = MaterialTheme.colorScheme.surface.copy(ContentAlpha.disabled),
            contentColor = MaterialTheme.colorScheme.onSurface,
            elevation = 0.dp,
            navigationIcon = {
                TopAppBarBackButton(
                    onClick = { onPopBackStack() }
                )
            },
            actions = {
                if (chapter != null) {
                    AppIconButton(
                        imageVector = if (vm.expandTopMenu) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
                        contentDescription = localize(Res.string.expand_menu),
                        onClick = {
                            vm.expandTopMenu = !vm.expandTopMenu
                        }
                    )
                    if (vm.expandTopMenu) {
                        // TTS Button (Desktop-specific)
                        TTSButton(onToggleTTSControls = onToggleTTSControls)
                        
                        AppIconButton(
                            imageVector = if (chapter.bookmark) Icons.Filled.Bookmark else Icons.Default.Bookmark,
                            contentDescription = localize(Res.string.bookmark),
                            tint = if (chapter.bookmark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            onClick = {
                                onBookMark()
                            }
                        )
                        AppIconButton(
                            imageVector = Icons.Default.Search,
                            contentDescription = localizeHelper.localize(Res.string.find_in_chapter),
                            onClick = {
                                vm.toggleFindInChapter()
                            }
                        )
                        AppIconButton(
                            imageVector = Icons.Default.Report,
                            contentDescription = localizeHelper.localize(Res.string.report_broken_chapter),
                            onClick = {
                                vm.toggleReportDialog()
                            }
                        )
                        AppIconButton(
                            imageVector = Icons.Default.BrightnessHigh,
                            contentDescription = localizeHelper.localize(Res.string.brightness),
                            onClick = {
                                vm.showBrightnessControl = !vm.showBrightnessControl
                            }
                        )
                        // Font size quick adjuster button
                        IconButton(
                            onClick = {
                                vm.showFontSizeAdjuster = !vm.showFontSizeAdjuster
                            }
                        ) {
                            Text(
                                text = localizeHelper.localize(Res.string.aa),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        if (!vm.webViewIntegration.value) {
                            AppIconButton(
                                imageVector = Icons.Default.Public,
                                contentDescription = localize(Res.string.webView),
                                onClick = {
                                    onWebView()
                                }
                            )
                        }
                    }
                    AppIconButton(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = localize(Res.string.refresh),
                        onClick = {
                            onRefresh()
                        }
                    )
                }
            }
        )
    }

    // Loading state top bar
    if (!isLoaded) {
        Toolbar(
            title = {},
            elevation = 0.dp,
            backgroundColor = vm.backgroundColor.value.toComposeColor().copy(ContentAlpha.disabled),
            contentColor = vm.textColor.value.toComposeColor(),
            actions = {
                AppIconButton(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = localize(Res.string.refresh),
                    onClick = {
                        onRefresh()
                    },
                    tint = vm.textColor.value.toComposeColor()
                )
            },
            navigationIcon = {
                TopAppBarBackButton(onClick = {
                    onPopBackStack()
                }, tint = vm.textColor.value.toComposeColor())
            }
        )
    }
}
