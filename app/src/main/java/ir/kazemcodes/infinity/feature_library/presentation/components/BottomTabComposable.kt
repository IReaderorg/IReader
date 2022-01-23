package ir.kazemcodes.infinity.feature_library.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import ir.kazemcodes.infinity.feature_library.presentation.LibraryViewModel
import kotlinx.coroutines.CoroutineScope

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun BottomTabComposable(modifier: Modifier = Modifier, viewModel: LibraryViewModel, pagerState: PagerState, scope: CoroutineScope) {
    val tabs = listOf(TabItem.Filter(viewModel = viewModel), TabItem.Sort(viewModel), TabItem.Display(viewModel = viewModel))

    ModalBottomSheetLayout(sheetBackgroundColor = MaterialTheme.colors.background,
        modifier = Modifier.height(500.dp),
        sheetContent = {
            /** There is Some issue here were sheet content is not need , not sure why**/
            Column(modifier = modifier.fillMaxSize()) {
                Tabs(libraryTabs = tabs, pagerState = pagerState)
                TabsContent(libraryTabs = tabs, pagerState = pagerState)

            }
        }, content = {
            Column(modifier = modifier.fillMaxSize()) {
                Tabs(libraryTabs = tabs, pagerState = pagerState)
                TabsContent(libraryTabs = tabs, pagerState = pagerState)
            }
        })

}



