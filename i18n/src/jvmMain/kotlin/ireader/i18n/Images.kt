package ireader.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.res.useResource
import org.xml.sax.InputSource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
            try {
                useResource(resourcePath) {
                    loadXmlImageVector(InputSource(it), density)
                }
            } catch (e: Exception) {
                // Log the error
                println("ERROR: Failed to load resource: $resourcePath")
                e.printStackTrace()
                
                // Return a fallback vector if the specific one fails
                // This creates a simple square as fallback
                androidx.compose.ui.graphics.vector.ImageVector.Builder(
                    defaultWidth = 64.dp,
                    defaultHeight = 64.dp,
                    viewportWidth = 64f,
                    viewportHeight = 64f
                ).addPath(
                    pathData = androidx.compose.ui.graphics.vector.PathData {
                        moveTo(0f, 0f)
                        lineTo(64f, 0f)
                        lineTo(64f, 64f)
                        lineTo(0f, 64f)
                        close()
                    },
                    fill = androidx.compose.ui.graphics.SolidColor(
                        androidx.compose.ui.graphics.Color.Gray
                    )
                ).build()
            }
        }
    }
    @Composable
    fun rememberVectorAnimatedResource(resourcePath: String): Painter {
        val density = LocalDensity.current
        return remember(resourcePath, density) {
            try {
                useResource(resourcePath) {
                    loadSvgPainter(it, density)
                }
            } catch (e: Exception) {
                // Return a fallback painter if loading fails
                println("ERROR: Failed to load resource: $resourcePath")
                androidx.compose.ui.graphics.painter.ColorPainter(
                    androidx.compose.ui.graphics.Color.Gray
                )
            }
        }
    }
    @Composable
    actual fun eternityLight(): ImageVector = rememberVectorXmlResource("drawable/ic_eternity_light.xml")


    @Composable
    actual fun discord(): ImageVector = rememberVectorXmlResource("drawable/ic_discord_24dp.xml")
    @Composable
    actual fun github(): ImageVector = rememberVectorXmlResource("drawable/ic_github_24dp.xml")

    @Composable
    actual fun incognito(): ImageVector  = rememberVectorXmlResource("drawable/ic_glasses_24dp.xml")

    @Composable
    actual fun arrowDown(atEnd: Boolean): Painter {
        return rememberVectorAnimatedResource("drawable/anim_caret_down.xml")
    }

}