package ireader.ui.video

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ireader.core.source.findInstance
import ireader.core.source.model.MovieUrl
import ireader.ui.video.component.VideoView

@Composable
fun VideoPresenter(
    vm: VideoScreenViewModel
) {
    val uri = remember(vm.chapter) {
        vm.chapter?.content?.findInstance<MovieUrl>()?.url ?: ""
    }
    VideoView(Uri.parse(uri))

}
