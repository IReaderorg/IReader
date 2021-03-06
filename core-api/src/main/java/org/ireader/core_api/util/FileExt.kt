package org.ireader.core_api.util

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**
 * Returns the uri of a file
 *
 * @param context context of application
 */
fun File.getUriCompat(context: Context): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        FileProvider.getUriForFile(context, "${context.packageName}.provider", this)
    else Uri.fromFile(this)
}

fun File.calculateSizeRecursively(): Long {
    return walkBottomUp().fold(0L) { acc, file -> acc + file.length() }
}
