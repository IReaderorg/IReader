package ireader.i18n

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector


expect object Images {

    @Composable
    fun downloading(): ImageVector

    @Composable
    fun close(): ImageVector

    @Composable
    fun update(): ImageVector

    @Composable
    fun skip(): ImageVector

    @Composable
    fun fastRewind(): ImageVector

    @Composable
    fun pause(): ImageVector

    @Composable
    fun playArrow(): ImageVector

    @Composable
    fun fastForward(): ImageVector

    @Composable
    fun skipNext(): ImageVector

    @Composable
    fun openInNw(): ImageVector

    @Composable
    fun infinity(): ImageVector

    @Composable
    fun skipPrevious(): ImageVector
    @Composable
    fun eternityLight(): ImageVector


}