package org.ireader.common_extensions

import android.content.Context
import android.content.Intent


fun launchMainActivityIntent(context:Context): Intent {
    val packageName = context.packageManager
    return packageName.getLaunchIntentForPackage(context.packageName)!!
}