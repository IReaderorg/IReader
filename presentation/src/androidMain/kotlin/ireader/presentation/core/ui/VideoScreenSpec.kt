package ireader.presentation.core.ui


import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NamedNavArgument
import ireader.domain.utils.extensions.*
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.video.VideoPlayerBottomSheet
import ireader.presentation.ui.video.VideoPresenter
import ireader.presentation.ui.video.VideoScreenViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


object VideoScreenSpec : ScreenSpec {
    override val navHostRoute: String = "video_screen_route/{chapterId}"

    fun buildRoute(
        chapterId: Long,
    ): String {
        return "video_screen_route/$chapterId"
    }

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.chapterId,
        NavigationArgs.showModalSheet,
    )


    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        val vm: VideoScreenViewModel =
            koinViewModel(viewModelStoreOwner = controller.navBackStackEntry)

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
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        SnackBarListener(vm = vm, host = controller.snackBarHostState)
        IModalSheets(bottomSheetState = sheetState,
        sheetContent = {
            val stateOfPlayer = state.playerState
            if (stateOfPlayer != null) {
                VideoPlayerBottomSheet(
                    playerState = stateOfPlayer,
                    mediaState = state,
                    vm = vm,
                    sheetState = sheetState
                )
            }
        }) {
            VideoPresenter(
                vm, onShowMenu = {
                    scope.launch {
                        sheetState.animateTo(ModalBottomSheetValue.Expanded)
                    }
                },
                state = state,
                player = state.player
            )
        }

    }
}
