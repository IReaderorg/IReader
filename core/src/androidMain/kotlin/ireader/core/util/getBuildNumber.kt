package ireader.core.util

import android.os.Build

actual fun getBuildNumber() : Int{
    return Build.VERSION.SDK_INT
}