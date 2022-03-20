package org.ireader.core.utils

import android.content.Context
import org.ireader.core.io.calculateSizeRecursively

fun getCacheSize(context: Context): String {
    val size = context.cacheDir.calculateSizeRecursively()
    return when (size) {
        in 0..1024 -> "$size byte"
        in 1024..1048576 -> "${size / 1024} Kb"
        else -> "${size / (1024 * 1024)} Mb"
    }

}