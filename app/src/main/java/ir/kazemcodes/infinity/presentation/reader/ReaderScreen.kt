package ir.kazemcodes.infinity.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.presentation.reader.components.ChaptersSliderComposable
import ir.kazemcodes.infinity.presentation.reader.components.ReaderSettingComposable
import ir.kazemcodes.infinity.presentation.reusable_composable.ErrorTextWithEmojis
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarBackButton


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
) {

    val viewModel = rememberService<ReaderScreenViewModel>()
    val backStack = LocalBackstack.current
    val state = viewModel.state.value
    val interactionSource = remember { MutableInteractionSource() }


    Box(modifier = modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                if (!state.isReaderModeEnable && state.isLoaded) {
                    TopAppBar(
                        title = {
                            Text(
                                text = viewModel.state.value.chapter.title,
                                color = MaterialTheme.colors.onBackground,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground,
                        elevation = 8.dp,
                        navigationIcon = {
                            TopAppBarBackButton(backStack = backStack)
                        },
                        actions = {
                            IconButton(onClick = { viewModel.getReadingContentRemotely(viewModel.state.value.chapter) }) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = "Refresh",
                                    tint = MaterialTheme.colors.onBackground,
                                )
                            }
                        }
                    )
                } else if (!state.isLoaded) {
                    TopAppBar(title = {},
                        elevation = 0.dp,
                        backgroundColor = MaterialTheme.colors.background,
                        navigationIcon = {
                            TopAppBarBackButton(backStack = backStack)
                        })
                }
            },
            bottomBar = {
                if (!state.isReaderModeEnable && state.isLoaded) {
                    BottomAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (viewModel.state.value.isMainBottomModeEnable) 130.dp else 320.dp),
                        elevation = 8.dp,
                        backgroundColor = MaterialTheme.colors.background
                    ) {
                        Column(modifier.fillMaxSize()) {
                            Divider(modifier =modifier.fillMaxWidth(),color=MaterialTheme.colors.onBackground.copy(alpha = .2f), thickness = 1.dp)
                            Spacer(modifier = modifier.height(15.dp))
                            if (viewModel.state.value.isMainBottomModeEnable) {
                                ChaptersSliderComposable( viewModel = viewModel, isChaptersReversed =viewModel.state.value.isChaptersReversed )
                                Row(modifier =modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                    TopAppBarActionButton(imageVector = Icons.Default.Menu,
                                        title = "Chapter List Drawer",
                                        onClick = {  })
                                    TopAppBarActionButton(imageVector = Icons.Default.Settings,
                                        title = "Setting Drawer",
                                        onClick = { viewModel.toggleSettingMode(true) })
                                }
                            }
                            if (viewModel.state.value.isSettingModeEnable) {
                                ReaderSettingComposable(viewModel = viewModel)
                            }
                            
                        }
                    }
                }


            }
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .clickable(interactionSource = interactionSource,
                        indication = null) { viewModel.onEvent(ReaderEvent.ToggleReaderMode(!state.isReaderModeEnable)) }
                    .background(viewModel.state.value.backgroundColor)
                    .padding(8.dp)
                    .wrapContentSize(Alignment.CenterStart)
            ) {
                if (state.chapter.isChapterNotEmpty()) {
                    Text(
                        modifier = modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart),
                        text = state.chapter.content.joinToString("\n".repeat(state.distanceBetweenParagraphs)),
                        fontSize = viewModel.state.value.fontSize.sp,
                        fontFamily = viewModel.state.value.font.fontFamily,
                        textAlign = TextAlign.Start,
                        color = state.textColor,
                        lineHeight = state.lineHeight.sp
                    )
                }
            }
        }
        if (state.error.isNotBlank()) {
            ErrorTextWithEmojis(error = state.error, modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .wrapContentSize(Alignment.Center)
                .align(Alignment.Center))
        }

        if (viewModel.state.value.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colors.primary
            )
        }

    }
}


