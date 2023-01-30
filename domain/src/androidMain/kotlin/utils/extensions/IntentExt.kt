package ireader.domain.utils.extensions

import android.content.Context
import android.content.Intent

fun launchMainActivityIntent(context: Context): Intent {
    val packageName = context.packageManager
    return packageName.getLaunchIntentForPackage(context.packageName)!!
}
