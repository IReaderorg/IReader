package ireader.presentation.core.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

actual fun currentBatteryLevel(platformContext: Any?): Int? {
    val context = platformContext as? Context ?: return null
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return null
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return if (level >= 0 && scale > 0) (level * 100) / scale else null
}
