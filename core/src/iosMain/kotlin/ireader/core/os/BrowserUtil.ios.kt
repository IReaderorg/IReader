package ireader.core.os

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openInBrowser(url: String): Result<Unit> {
    return try {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null && UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            UIApplication.sharedApplication.openURL(nsUrl)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Cannot open URL: $url"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
