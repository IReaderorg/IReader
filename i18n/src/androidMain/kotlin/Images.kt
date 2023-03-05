package ireader.i18n

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

actual object Images {
    @Composable
    actual fun downloading(): ImageVector = ImageVector.vectorResource(R.drawable.ic_downloading)

    @Composable
    actual fun close(): ImageVector = ImageVector.vectorResource(R.drawable.baseline_close_24)

    @Composable
    actual fun update(): ImageVector = ImageVector.vectorResource(R.drawable.ic_update)

    @Composable
    actual fun skip(): ImageVector = ImageVector.vectorResource(R.drawable.ic_baseline_skip_next)

    @Composable
    actual fun fastRewind(): ImageVector = ImageVector.vectorResource(R.drawable.ic_baseline_fast_rewind)

    @Composable
    actual fun pause(): ImageVector = ImageVector.vectorResource(R.drawable.ic_baseline_pause)

    @Composable
    actual fun playArrow(): ImageVector = ImageVector.vectorResource(R.drawable.ic_baseline_play_arrow)

    @Composable
    actual fun fastForward(): ImageVector = ImageVector.vectorResource(R.drawable.ic_baseline_fast_forward)

    @Composable
    actual fun skipNext(): ImageVector = ImageVector.vectorResource(R.drawable.ic_baseline_skip_next)

    @Composable
    actual fun openInNw(): ImageVector = ImageVector.vectorResource(R.drawable.ic_baseline_open_in_new_24)

    @Composable
    actual fun infinity(): ImageVector = ImageVector.vectorResource(R.drawable.ic_infinity)

    @Composable
    actual fun skipPrevious(): ImageVector = ImageVector.vectorResource(R.drawable.ic_baseline_skip_previous)
    @Composable
    actual fun eternityLight(): ImageVector = ImageVector.vectorResource(R.drawable.ic_eternity_light)


    @Composable
    actual fun discord(): ImageVector = ImageVector.vectorResource(R.drawable.ic_discord_24dp)
    @Composable
    actual fun github(): ImageVector = ImageVector.vectorResource(R.drawable.ic_github_24dp)
}