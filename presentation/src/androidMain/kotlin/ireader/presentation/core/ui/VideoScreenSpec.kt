package ireader.presentation.core.ui


import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.utils.extensions.hideSystemUI
import ireader.domain.utils.extensions.showSystemUI
import ireader.domain.utils.findComponentActivity
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.video.VideoPlayerBottomSheet
import ireader.presentation.ui.video.VideoPresenter
import ireader.presentation.ui.video.VideoScreenViewModel
import kotlinx.coroutines.launch



actual data class VideoScreenSpec actual constructor(val chapterId: Long) : VoyagerScreen() {


    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        val vm: VideoScreenViewModel =
            getIViewModel(parameters = VideoScreenViewModel.Param(chapterId))

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
                    modifier = it,
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
                        sheetState.show()
                    }
                },
                state = state,
                player = state.player
            )
        }

    }
}
