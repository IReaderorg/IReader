package ireader.presentation.ui


import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import ireader.presentation.ui.util.NavigationArgs
import ireader.ui.component.Controller
import ireader.ui.video.VideoPresenter
import ireader.ui.video.VideoScreenViewModel
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
        )

    @Composable
    override fun TopBar(controller: Controller) {

    }


    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: VideoScreenViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            parametersOf(
                VideoScreenViewModel.createParam(controller)
            )
        })
        VideoPresenter(vm)

    }

}
