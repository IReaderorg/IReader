package ireader.presentation.core.ui


import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NamedNavArgument
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.utils.extensions.*
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.video.VideoPlayerBottomSheet
import ireader.presentation.ui.video.VideoPresenter
import ireader.presentation.ui.video.VideoScreenViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


data class VideoScreenSpec(val chapterId: Long) : VoyagerScreen() {


    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        val vm: VideoScreenViewModel =
            getIViewModel()

        val state = vm.mediaState
        DisposableEffect(key1 = true) {
            context.findComponentActivity()?.let { activity ->
                activity.findComponentActivity()?.hideSystemUI()
            }
            onDispose {
                context.findComponentActivity()?.let { activity ->
                    activity.findComponentActivity()?.showSystemUI()
                }
            }
        }
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val snackBarHostState = SnackBarListener(vm = vm)
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
            IScaffold(

                        snackbarHostState = snackBarHostState
            ) {

            }
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
