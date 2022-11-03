package ireader.presentation.core.ui


import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NamedNavArgument
import ireader.domain.utils.extensions.*
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.video.VideoPlayerBottomSheet
import ireader.presentation.ui.video.VideoPresenter
import ireader.presentation.ui.video.VideoScreenViewModel
import ireader.presentation.ui.video.component.core.rememberManagedExoPlayer
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

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
        val context = LocalContext.current
        val player  : ExoPlayer? = rememberManagedExoPlayer()
        val vm: VideoScreenViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            parametersOf(player)
        })
        //val state = rememberMediaState(player = vm.player.value, source = vm.source as? HttpSource)
        val state = vm.mediaState
        DisposableEffect(key1 = true) {
            controller.requestedHideSystemStatusBar(true)
            context.findComponentActivity()?.let { activity ->
                activity.findComponentActivity()?.hideSystemUI()
            }
            onDispose {
                context.findComponentActivity()?.let { activity ->
                    activity.findComponentActivity()?.showSystemUI()
                }
                controller.requestedHideSystemStatusBar(false)
            }
        }


        SnackBarListener(vm = vm, host = controller.snackBarHostState)
        VideoPresenter(vm, onShowMenu = {
            scope.launch {
                controller.sheetState.animateTo(ModalBottomSheetValue.Expanded)
            }
        },
                state = state,
                player = player)

    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun BottomModalSheet(controller: Controller) {
        val vm: VideoScreenViewModel = getViewModel(owner = controller.navBackStackEntry)
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val mediaState = vm.mediaState
        val playerState = mediaState?.playerState
        if (mediaState != null && playerState != null) {
            mediaState?.let { media ->
                VideoPlayerBottomSheet(playerState = playerState, mediaState = media, controller = controller, vm = vm)
            }
        }
    }
}
