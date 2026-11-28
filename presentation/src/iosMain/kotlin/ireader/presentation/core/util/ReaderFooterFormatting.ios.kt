package ireader.presentation.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIDevice

@OptIn(ExperimentalForeignApi::class)
actual fun currentBatteryLevel(platformContext: Any?): Int? {
    val device = UIDevice.currentDevice
    device.batteryMonitoringEnabled = true
    val level = device.batteryLevel
    return if (level < 0f) null else (level * 100f).toInt()
}
