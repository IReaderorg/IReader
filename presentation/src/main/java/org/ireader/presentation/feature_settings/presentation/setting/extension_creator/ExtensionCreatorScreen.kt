package org.ireader.presentation.feature_settings.presentation.setting.extension_creator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import org.ireader.core.utils.UiEvent
import org.ireader.presentation.feature_library.presentation.components.TabItem
import org.ireader.presentation.feature_library.presentation.components.Tabs
import org.ireader.presentation.feature_library.presentation.components.TabsContent
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton

@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ExtensionCreatorScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: ExtensionCreatorViewModel = hiltViewModel(),
) {
    val state = viewModel.state.value
    val pagerState = rememberPagerState()
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
            }
        }

    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                MidSizeTextComposable(title = "Extension Creator")
            },
            backgroundColor = MaterialTheme.colors.background,
            navigationIcon = {
                TopAppBarBackButton(navController = navController)
            },
            actions = {
//                SmallTextComposable(title = "Format",
//                    modifier = modifier.clickable { viewModel.formatJson() })
                TopAppBarActionButton(imageVector = Icons.Default.Add,
                    title = "Adding Sources Button",
                    onClick = { viewModel.convertJsonToSource() })
            }
        )
    },
        scaffoldState = scaffoldState, snackbarHost = { ISnackBarHost(snackBarHostState = it) }) {
        val tabs = listOf<TabItem>(TabItem.ExtensionCreator(viewModel),
            TabItem.ExtensionCreatorLog(viewModel))
        Column(modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
            Tabs(libraryTabs = tabs, pagerState = pagerState)
            TabsContent(libraryTabs = tabs, pagerState = pagerState)
        }
    }
}