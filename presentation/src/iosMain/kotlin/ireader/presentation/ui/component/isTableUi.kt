package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIDeviceOrientation

@Composable
actual fun isTableUi(): Boolean {
    return UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad
}

@Composable
actual fun isLandscape(): Boolean {
    val orientation = UIDevice.currentDevice.orientation
    return orientation == UIDeviceOrientation.UIDeviceOrientationLandscapeLeft ||
           orientation == UIDeviceOrientation.UIDeviceOrientationLandscapeRight
}
