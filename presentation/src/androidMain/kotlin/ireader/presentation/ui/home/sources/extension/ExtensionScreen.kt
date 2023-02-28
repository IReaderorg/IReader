package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import ireader.domain.models.entities.Catalog
import ireader.i18n.UiEvent
import ireader.i18n.asString
import ireader.presentation.R
import ireader.presentation.ui.component.components.component.pagerTabIndicatorOffset
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.utils.horizontalPadding
import ireader.presentation.ui.home.sources.extension.composables.RemoteSourcesScreen
import ireader.presentation.ui.home.sources.extension.composables.UserSourcesScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExtensionScreen(
    modifier: Modifier = Modifier,
    vm: ExtensionViewModel,
    onClickCatalog: (Catalog) -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
    snackBarHostState: androidx.compose.material3.SnackbarHostState,
) {

    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
                else -> {}
            }
        }
    }
    val pages = remember {
        listOf<String>(
            context.getString(R.string.sources),
            context.getString(R.string.extensions),
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        ExtensionContent(
            vm = vm,
            state = vm,
            onClickCatalog = onClickCatalog,
            onClickInstall = onClickInstall,
            onClickTogglePinned = onClickTogglePinned,
            onClickUninstall = onClickUninstall,
            pages = pages,
            onCancelInstaller = onCancelInstaller,
        )
    }
}

@Composable
fun SourceHeader(
    modifier: Modifier = Modifier,
    language: String,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = LocaleHelper.getSourceDisplayName(language, context),
            modifier = modifier
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ExtensionContent(
    pages: List<String>,
    modifier: Modifier = Modifier,
    state: CatalogsState,
    onClickCatalog: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    vm: ExtensionViewModel,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,

    ) {
    val pagerState = rememberPagerState()
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            vm.currentPagerPage = pagerState.currentPage
        }
    }
    ExtensionTabs(pagerState = pagerState, pages = pages)
    ExtensionPager(
        pagerState = pagerState,
        vm = vm,
        state = vm,
        onClickCatalog = onClickCatalog,
        onClickInstall = onClickInstall,
        onClickTogglePinned = onClickTogglePinned,
        onClickUninstall = onClickUninstall,
        pages = pages,
        onCancelInstaller = onCancelInstaller
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ExtensionPager(
    pagerState: PagerState,
    pages: List<String>,
    modifier: Modifier = Modifier,
    state: CatalogsState,
    onClickCatalog: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    vm: ExtensionViewModel,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
) {
    HorizontalPager(
        count = pages.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        when (page) {
            0 -> {
                UserSourcesScreen(
                    onClickCatalog = onClickCatalog,
                    onClickTogglePinned = onClickTogglePinned,
                    vm = vm,
                )
            }
            1 -> {
                RemoteSourcesScreen(
                    vm = vm,
                    onClickInstall = onClickInstall,
                    onClickUninstall = onClickUninstall,
                    onCancelInstaller = onCancelInstaller,
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ExtensionTabs(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    pages: List<String>,

    ) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = AppColors.current.bars,
        contentColor = AppColors.current.onBars,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = MaterialTheme.colorScheme.primary,
            )
        },
    ) {
        pages.forEachIndexed { index, title ->
            Tab(
                text = {
                    MidSizeTextComposable(text = title, color = Color.Unspecified)
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
