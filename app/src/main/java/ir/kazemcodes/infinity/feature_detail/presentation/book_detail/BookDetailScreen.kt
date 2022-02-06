package ir.kazemcodes.infinity.feature_detail.presentation.book_detail


import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ir.kazemcodes.infinity.core.presentation.components.ISnackBarHost
import ir.kazemcodes.infinity.core.presentation.reusable_composable.ErrorTextWithEmojis
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.core.ui.WebViewScreenSpec
import ir.kazemcodes.infinity.core.utils.getUrlWithoutDomain
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType


@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: BookDetailViewModel = hiltViewModel(),
) {


    val context = LocalContext.current


    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.state.isLocalLoaded) {
            BookDetailScreenLoadedComposable(
                modifier = modifier,
                viewModel = viewModel,
                navController = navController
            )
        } else {
            Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                TopAppBar(title = {},
                    backgroundColor = MaterialTheme.colors.background,
                    navigationIcon = {
                        TopAppBarBackButton(navController = navController)
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(WebViewScreenSpec.buildRoute(
                                url = viewModel.state.source.baseUrl + getUrlWithoutDomain(
                                    viewModel.state.book.link),
                                sourceId = viewModel.state.source.sourceId,
                                fetchType = FetchType.Detail.index,
                                bookId = viewModel.state.book.id
                            ))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "WebView",
                                tint = MaterialTheme.colors.onBackground,
                            )
                        }
                    }
                )

            },
                snackbarHost = {
                    ISnackBarHost(it)
                }

            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (viewModel.state.error.asString(context).isNotBlank()) {
                        ErrorTextWithEmojis(error = viewModel.state.error.asString(context), modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .wrapContentSize(Alignment.Center)
                            .align(Alignment.Center))
                    }
                    if (viewModel.state.isLocalLoading || viewModel.state.isRemoteLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }


    }


}


