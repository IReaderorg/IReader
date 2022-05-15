package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.TextButton
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.ireader.chapterDetails.ChapterDetailScreen
import org.ireader.chapterDetails.ChapterDetailTopAppBar
import org.ireader.chapterDetails.viewmodel.ChapterDetailEvent
import org.ireader.chapterDetails.viewmodel.ChapterDetailViewModel
import org.ireader.common_resources.UiText
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.domain.ui.NavigationArgs
import org.ireader.ui_chapter_detail.R

object ChapterScreenSpec : ScreenSpec {

    override val navHostRoute: String = "chapter_detail_route/{bookId}/{sourceId}"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.bookId,
        NavigationArgs.sourceId,
        NavigationArgs.haveDrawer,
    )

    fun buildRoute(
        bookId: Long,
        sourceId: Long,
    ): String {
        return "chapter_detail_route/$bookId/$sourceId"
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: ChapterDetailViewModel = hiltViewModel(navBackStackEntry)
        val scrollState = vm.scrollState
        val scope = rememberCoroutineScope()
        ChapterDetailTopAppBar(
            state = vm,
            onClickCancelSelection = { vm.selection.clear() },
            onClickSelectAll = {
                vm.selection.clear()
                vm.selection.addAll(vm.chapters.map { it.id })
                vm.selection.distinct()
            },
            onClickFlipSelection = {
                val ids: List<Long> =
                    vm.chapters.map { it.id }
                        .filterNot { it in vm.selection }.distinct()
                vm.selection.clear()
                vm.selection.addAll(ids)
            },
            onReverseClick = {
                vm.onEvent(ChapterDetailEvent.ToggleOrder)
            },
            onPopBackStack = {
                navController.popBackStack()
            },
            onMap = {
                scope.launch {
                    try {
                        scrollState?.scrollToItem(
                            vm.getLastChapterIndex(),
                            -scrollState.layoutInfo.viewportEndOffset / 2
                        )
                    } catch (e: Throwable) {
                    }
                }
            }
        )
    }
    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun ModalDrawer(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: ChapterDetailViewModel = hiltViewModel(navBackStackEntry)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            BigSizeTextComposable(text = stringResource(R.string.advance_setting))

            Spacer(modifier = Modifier.height(5.dp))
            Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.reverseChapterInDB() }
            ) {
                MidSizeTextComposable(text = UiText.StringResource(R.string.reverse_chapter_in_db))
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.autoSortChapterInDB() }
            ) {
                MidSizeTextComposable(text = UiText.StringResource(R.string.auto_sort_chapters_in_db))
            }
        }
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding: PaddingValues,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: ChapterDetailViewModel = hiltViewModel(navBackStackEntry)
        val book = vm.book
        ChapterDetailScreen(
            onItemClick = { index ->
                if (vm.selection.isEmpty()) {
                    if (book != null) {
                        navController.navigate(
                            ReaderScreenSpec.buildRoute(
                                bookId = book.id,
                                sourceId = book.sourceId,
                                chapterId = vm.chapters[index].id,
                            )
                        )
                    }
                } else {
                    when (vm.chapters[index].id) {
                        in vm.selection -> {
                            vm.selection.remove(vm.chapters[index].id)
                        }
                        else -> {
                            vm.selection.add(vm.chapters[index].id)
                        }
                    }
                }
            },
            onLongItemClick = { index ->
                when (vm.chapters[index].id) {
                    in vm.selection -> {
                        vm.selection.remove(vm.chapters[index].id)
                    }
                    else -> {
                        vm.selection.add(vm.chapters[index].id)
                    }
                }
            },
            vm = vm,
            scaffoldPadding = scaffoldPadding
        )
    }
}
