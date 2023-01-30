package ireader.presentation.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.R
import ireader.presentation.ui.reader.viewmodel.ReaderScreenState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreenTopBar(
        modifier: Modifier,
        isReaderModeEnable: Boolean,
        vm: ReaderScreenViewModel,
        state: ReaderScreenState,
        modalBottomSheetValue: ModalBottomSheetValue,
        chapter: Chapter?,
        onRefresh: () -> Unit,
        onWebView: () -> Unit,
        onBookMark: () -> Unit,
        onPopBackStack: () -> Unit,
        isLoaded: Boolean = false,
) {

    AnimatedVisibility(
        visible = !isReaderModeEnable,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(250)),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(250))
    ) {
        Toolbar(
            modifier = modifier,
            title = {
                Text(
                    text = chapter?.name ?: "",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
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
                        contentDescription = stringResource(R.string.expand_menu),
                        onClick = {
                            vm.expandTopMenu = !vm.expandTopMenu
                        }
                    )
                    if (vm.expandTopMenu) {
                        AppIconButton(
                            imageVector = if (chapter.bookmark) Icons.Filled.Bookmark else Icons.Default.Bookmark,
                            contentDescription = stringResource(R.string.bookmark),
                            tint = if (chapter.bookmark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            onClick = {
                                onBookMark()
                            }
                        )
                        AppIconButton(
                            imageVector = Icons.Default.Public,
                            contentDescription = stringResource(R.string.webView),
                            onClick = {
                                onWebView()
                            }
                        )
                    }
                    AppIconButton(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = stringResource(R.string.refresh),
                        onClick = {
                            onRefresh()
                        }
                    )
                }
            }
        )
    }

    if (!isLoaded) {
        Toolbar(
            title = {},
            elevation = 0.dp,
            backgroundColor = vm.backgroundColor.value,
            actions = {
                if (chapter != null) {
                    AppIconButton(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = stringResource(R.string.refresh),
                        onClick = {
                            onRefresh()
                        },
                        tint = vm.textColor.value
                    )
                    AppIconButton(
                        imageVector = Icons.Default.Public,
                        contentDescription = stringResource(R.string.webView),
                        onClick = {
                            onWebView()
                        },
                        tint = vm.textColor.value
                    )
                }
            },
            navigationIcon = {
                TopAppBarBackButton(onClick = {
                    onPopBackStack()
                }, tint = vm.textColor.value)
            }
        )
    }
}
