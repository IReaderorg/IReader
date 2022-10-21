package ireader.presentation.core.ui


import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.video.VideoPresenter
import ireader.presentation.ui.video.VideoScreenViewModel
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
        )

    @Composable
    override fun TopBar(controller: Controller) {

    }


    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: VideoScreenViewModel = getViewModel(owner = controller.navBackStackEntry)
        VideoPresenter(vm)

    }

}
