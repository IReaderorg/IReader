package ireader.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.res.useResource
import org.xml.sax.InputSource

actual object Images {
    @Composable
    actual fun downloading(): ImageVector = rememberVectorXmlResource("drawable/ic_downloading.xml")

    @Composable
    actual fun close(): ImageVector = rememberVectorXmlResource("drawable/baseline_close_24.xml")

    @Composable
    actual fun update(): ImageVector = rememberVectorXmlResource("drawable/ic_update.xml")

    @Composable
    actual fun skip(): ImageVector = rememberVectorXmlResource("drawable/ic_baseline_skip_next.xml")

    @Composable
    actual fun fastRewind(): ImageVector = rememberVectorXmlResource("drawable/ic_baseline_fast_rewind.xml")

    @Composable
    actual fun pause(): ImageVector = rememberVectorXmlResource("drawable/ic_baseline_pause.xml")

    @Composable
    actual fun playArrow(): ImageVector = rememberVectorXmlResource("drawable/ic_baseline_play_arrow.xml")

    @Composable
    actual fun fastForward(): ImageVector = rememberVectorXmlResource("drawable/ic_baseline_fast_forward.xml")

    @Composable
    actual fun skipNext(): ImageVector = rememberVectorXmlResource("drawable/ic_baseline_skip_next.xml")

    @Composable
    actual fun openInNw(): ImageVector = rememberVectorXmlResource("drawable/ic_baseline_open_in_new_24.xml")

    @Composable
    actual fun infinity(): ImageVector = rememberVectorXmlResource("drawable/ic_infinity.xml")

    @Composable
    actual fun skipPrevious(): ImageVector = rememberVectorXmlResource("drawable/ic_baseline_skip_previous.xml")
    @Composable
    private fun rememberVectorXmlResource(resourcePath: String): ImageVector {
        val density = LocalDensity.current
        return remember(resourcePath, density) {
            useResource(resourcePath) {
                loadXmlImageVector(InputSource(it), density)
            }
        }
    }
    @Composable
    actual fun eternityLight(): ImageVector = rememberVectorXmlResource("drawable/ic_eternity_light.xml")

    @Composable
    actual fun discord(): ImageVector = rememberVectorXmlResource("drawable/ic_discord_24dp.xml")
    @Composable
    actual fun github(): ImageVector = rememberVectorXmlResource("drawable/ic_github_24dp.xml")

}