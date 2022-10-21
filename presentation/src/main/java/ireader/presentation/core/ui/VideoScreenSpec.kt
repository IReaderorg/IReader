package ireader.presentation.core.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NamedNavArgument
import ireader.i18n.UiText
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.component.ChoicePreference
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.video.VideoPresenter
import ireader.presentation.ui.video.VideoScreenViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

object VideoScreenSpec : ScreenSpec {
    override val navHostRoute: String = "video_screen_route/{chapterId}"

    fun buildRoute(
        chapterId: Long,
    ): String {
        return "video_screen_route/$chapterId"
    }

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.chapterId,
        NavigationArgs.showModalSheet
    )


    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val scope = rememberCoroutineScope()
        val vm: VideoScreenViewModel = getViewModel(owner = controller.navBackStackEntry)
        SnackBarListener(vm = vm, host = controller.snackBarHostState )
        VideoPresenter(vm, onShowMenu = {
            scope.launch {
                controller.sheetState.animateTo(ModalBottomSheetValue.Expanded)
                //controller.sheetState.show()
            }
        })

    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun BottomModalSheet(controller: Controller) {
        val vm: VideoScreenViewModel = getViewModel(owner = controller.navBackStackEntry)
        val scope = rememberCoroutineScope()
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White),) {
            LazyColumn {
                item {
                    ChoicePreference(preference = vm.playbackspeed, choices = mapOf(
                        .5f to "0.5x",
                        .75f to "0.75x",
                        1f to "1x",
                        1.25f to "1.25x",
                        1.50f to "1.5x",
                        1.75f to "1.75x",
                        2f to "2x",
                    ), title = "Play BackSpeed", onValue = {
                        vm.playbackspeed.value = it
                        vm.player.setPlaybackSpeed(it)
                        scope.launch {
                            controller.sheetState.hide()
                        }
                        vm.showSnackBar(UiText.DynamicString("Playback speed: $it."))
                    })
                }
                item {
                    ChoicePreference(preference = vm.playbackspeed, choices = mapOf(
                        .5f to "0.5x",
                        .75f to "0.75x",
                        1f to "1x",
                        1.25f to "1.25x",
                        1.50f to "1.5x",
                        1.75f to "1.75x",
                        2f to "2x",
                    ), title = "Subtitles", onValue = {
                        vm.playbackspeed.value = it
                        vm.player.setPlaybackSpeed(it)
                        scope.launch {
                            controller.sheetState.hide()
                        }
                        vm.showSnackBar(UiText.DynamicString("Playback speed: $it."))
                    })
                }
            }
        }


    }
}
