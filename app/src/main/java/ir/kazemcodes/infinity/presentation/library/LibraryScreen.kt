package ir.kazemcodes.infinity.presentation.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.base_feature.navigation.BookDetailKey
import ir.kazemcodes.infinity.library_feature.util.mappingApiNameToAPi
import ir.kazemcodes.infinity.presentation.browse.LayoutType
import ir.kazemcodes.infinity.presentation.components.GridLayoutComposable
import ir.kazemcodes.infinity.presentation.components.LinearViewList
import ir.kazemcodes.infinity.presentation.components.TitleText
import ir.kazemcodes.infinity.presentation.library.components.LibraryEvents
import ir.kazemcodes.infinity.presentation.library.components.RadioButtonWithTitleComposable
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun LibraryScreen(
) {
    val viewModel = rememberService<LibraryViewModel>()
    val backStack = LocalBackstack.current
    val state = viewModel.state.value
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (!state.inSearchMode) {
                            Text(
                                text = "Library",
                                color = MaterialTheme.colors.onBackground,
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            Box {
                                if (state.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Searching...",
                                        style = MaterialTheme.typography.subtitle1,
                                        color= MaterialTheme.colors.onBackground.copy(alpha = .7F)
                                    )
                                }
                                BasicTextField(
                                    value = state.searchQuery,
                                    onValueChange = {
                                        viewModel.onEvent(LibraryEvents.UpdateSearchInput(it))
                                    },
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = {
                                        viewModel.onEvent(LibraryEvents.SearchBooks(state.searchQuery))
                                    })
                                )
                            }
                        }

                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.onBackground,
                    elevation = 8.dp,
                    actions = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                    } else {
                                        bottomSheetScaffoldState.bottomSheetState.collapse()
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Filter Icon",
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.onEvent(LibraryEvents.ToggleSearchMode())
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                            )
                        }

                    },
                    navigationIcon = if (state.inSearchMode) {
                        {
                            IconButton(onClick = {
                                viewModel.onEvent(LibraryEvents.ToggleSearchMode(false))
                            }) {
                                Icon(imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Arrow Back Icon")
                            }
                        }
                    } else null

                )
            },
            sheetContent = {
                ModalBottomSheetLayout(modifier = Modifier.height(500.dp), sheetContent = {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(MaterialTheme.colors.background)
                            .padding(12.dp)
                    ) {
                        TitleText(text = "Display")
                        RadioButtonWithTitleComposable(
                            text = DisplayMode.CompactModel.title,
                            selected = viewModel.state.value.layout == LayoutType.CompactLayout,
                            onClick = { viewModel.onEvent(LibraryEvents.UpdateLayoutType(LayoutType.CompactLayout)) }
                        )
                        RadioButtonWithTitleComposable(
                            text = DisplayMode.GridLayout.title,
                            selected = viewModel.state.value.layout == LayoutType.GridLayout,
                            onClick = { viewModel.onEvent(LibraryEvents.UpdateLayoutType(LayoutType.GridLayout)) }
                        )
                    }

                }) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(MaterialTheme.colors.background)
                            .padding(12.dp)
                    ) {
                        TitleText(text = "Display")
                        RadioButtonWithTitleComposable(
                            text = DisplayMode.CompactModel.title,
                            selected = viewModel.state.value.layout == LayoutType.CompactLayout,
                            onClick = { viewModel.onEvent(LibraryEvents.UpdateLayoutType(LayoutType.CompactLayout)) }
                        )
                        RadioButtonWithTitleComposable(
                            text = DisplayMode.GridLayout.title,
                            selected = viewModel.state.value.layout == LayoutType.GridLayout,
                            onClick = { viewModel.onEvent(LibraryEvents.UpdateLayoutType(LayoutType.GridLayout)) }
                        )
                    }
                }

            },
            scaffoldState = bottomSheetScaffoldState
        ) {
            if (state.books.isNotEmpty()) {
                if (state.layout == LayoutType.GridLayout) {
                    GridLayoutComposable(books = if ( state.searchedBook.isEmpty()) state.books else state.searchedBook, onClick = { index ->
                        backStack.goTo(
                            BookDetailKey(
                                if (state.searchedBook.isEmpty()) state.books[index] else state.searchedBook[index],
                                source = mappingApiNameToAPi(if (state.searchedBook.isEmpty()) state.books[index].source?: "" else state.searchedBook[index].source ?: "")
                            )
                        )
                    })
                } else if (state.layout == LayoutType.CompactLayout) {
                    LinearViewList(books = state.books, onClick = { index ->
                        backStack.goTo(
                            BookDetailKey(
                                state.books[index],
                                source = mappingApiNameToAPi(state.books[index].source ?: "")
                            )
                        )
                    })
                }


            }

            if (state.error.isNotBlank()) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colors.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .align(Alignment.Center)
                )
            }
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }


}

