package ireader.i18n

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.resources.painterResource
import ireader.i18n.resources.*

actual object Images {
    @Composable
    actual fun downloading(): ImageVector = vectorResource(Res.drawable.ic_downloading)

    @Composable
    actual fun close(): ImageVector = vectorResource(Res.drawable.baseline_close_24)

    @Composable
    actual fun update(): ImageVector = vectorResource(Res.drawable.ic_update)

    @Composable
    actual fun skip(): ImageVector = vectorResource(Res.drawable.ic_baseline_skip_next)

    @Composable
    actual fun fastRewind(): ImageVector = vectorResource(Res.drawable.ic_baseline_fast_rewind)

    @Composable
    actual fun pause(): ImageVector = vectorResource(Res.drawable.ic_baseline_pause)

    @Composable
    actual fun playArrow(): ImageVector = vectorResource(Res.drawable.ic_baseline_play_arrow)

    @Composable
    actual fun fastForward(): ImageVector = vectorResource(Res.drawable.ic_baseline_fast_forward)

    @Composable
    actual fun skipNext(): ImageVector = vectorResource(Res.drawable.ic_baseline_skip_next)

    @Composable
    actual fun openInNw(): ImageVector = vectorResource(Res.drawable.ic_baseline_open_in_new_24)

    @Composable
    actual fun infinity(): ImageVector = vectorResource(Res.drawable.ic_infinity)

    @Composable
    actual fun skipPrevious(): ImageVector = vectorResource(Res.drawable.ic_baseline_skip_previous)
    
    @Composable
    actual fun eternityLight(): ImageVector = vectorResource(Res.drawable.ic_eternity_light)

    @Composable
    actual fun discord(): ImageVector = vectorResource(Res.drawable.ic_discord_24dp)
    
    @Composable
    actual fun github(): ImageVector = vectorResource(Res.drawable.ic_github_24dp)

    @Composable
    actual fun incognito(): ImageVector = vectorResource(Res.drawable.ic_glasses_24dp)

    @Composable
    actual fun arrowDown(atEnd: Boolean): Painter {
        return painterResource(Res.drawable.anim_caret_down)
    }

}