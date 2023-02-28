package ireader.core.os

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun checkNotificationPermission(context: Context, onPermissionNotGranted: () -> Unit = {},  onPermissionGranted: () -> Unit = {},) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        onPermissionNotGranted()
    } else {
        onPermissionGranted()
    }
}