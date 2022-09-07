package ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import androidx.navigation.NamedNavArgument
import kotlinx.coroutines.launch
import ireader.ui.chapter.ChapterDetailScreen
import ireader.ui.chapter.ChapterDetailTopAppBar
import ireader.ui.chapter.viewmodel.ChapterDetailViewModel
import ireader.ui.component.Controller
import ireader.ui.component.reusable_composable.BigSizeTextComposable
import ireader.ui.component.reusable_composable.MidSizeTextComposable
import ireader.domain.ui.NavigationArgs
import ireader.ui.book.viewmodel.BookDetailViewModel
import ireader.ui.book.viewmodel.BookDetailViewModel.Companion.createParam
import ireader.ui.chapter.R
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

object ChapterScreenSpec : ScreenSpec {

    override val navHostRoute: String = "chapter_detail_route/{bookId}/{sourceId}"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.bookId,
        NavigationArgs.sourceId,
        NavigationArgs.haveDrawer,
        NavigationArgs.showModalSheet,
    )

    fun buildRoute(
        bookId: Long,
        sourceId: Long,
    ): String {
        return "chapter_detail_route/$bookId/$sourceId"
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: ChapterDetailViewModel = getViewModel(owner = controller.navBackStackEntry)
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
                scope.launch {
                    controller.sheetState.show()
                }
            },
            scrollBehavior = controller.scrollBehavior,
            onPopBackStack = {
                controller.navController.popBackStack()
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
            },
            onSelectBetween = {
                val ids: List<Long> =
                    vm.chapters.map { it.id }
                        .filter { it in vm.selection }.distinct().sortedBy { it }.let {
                            val list = mutableListOf<Long>()
                            val min = it.minOrNull() ?: 0
                            val max = it.maxOrNull() ?: 0
                            for (id in min..max) {
                                list.add(id)
                            }
                            list
                        }
                vm.selection.clear()
                vm.selection.addAll(ids)
            }
        )
    }

    @Composable
    override fun ModalDrawer(
        controller: Controller
    ) {
        val vm: ChapterDetailViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                ChapterDetailViewModel.createParam(controller)
            )
        })
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
                MidSizeTextComposable(text = stringResource(R.string.reverse_chapter_in_db))
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.autoSortChapterInDB() }
            ) {
                MidSizeTextComposable(text = stringResource(R.string.auto_sort_chapters_in_db))
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class,)
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: ChapterDetailViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                ChapterDetailViewModel.createParam(controller)
            )
        })

        val book = vm.book
        ChapterDetailScreen(
            modifier = Modifier,
            onItemClick = { chapter ->
                if (vm.selection.isEmpty()) {
                    if (book != null) {
                        controller.navController.navigate(
                            ReaderScreenSpec.buildRoute(
                                bookId = book.id,
                                sourceId = book.sourceId,
                                chapterId = chapter.id,
                            )
                        )
                    }
                } else {
                    when (chapter.id) {
                        in vm.selection -> {
                            vm.selection.remove(chapter.id)
                        }
                        else -> {
                            vm.selection.add(chapter.id)
                        }
                    }
                }
            },
            onLongItemClick = { chapter ->
                when (chapter.id) {
                    in vm.selection -> {
                        vm.selection.remove(chapter.id)
                    }
                    else -> {
                        vm.selection.add(chapter.id)
                    }
                }
            },
            vm = vm,
            scaffoldPadding = controller.scaffoldPadding,
        )
    }

//    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
//    @Composable
//    override fun BottomModalSheet(
//        controller: Controller
//    ) {
//        val vm: ChapterDetailViewModel
//
//        val pagerState = rememberPagerState()
//        ChapterScreenBottomTabComposable(
//            pagerState = pagerState,
//            filters = vm.filters.value,
//            toggleFilter = {
//                vm.toggleFilter(it.type)
//            },
//            onSortSelected = {
//                vm.toggleSort(it.type)
//            },
//            sortType = vm.sorting.value,
//            isSortDesc = vm.isAsc,
//            onLayoutSelected = { layout ->
//                vm.layout = layout
//            },
//            layoutType = vm.layout,
//            vm = vm
//        )
//    }
}
