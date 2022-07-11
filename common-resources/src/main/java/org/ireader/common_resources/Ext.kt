package org.ireader.common_resources

import android.content.Context

fun Context.string(id: Int): String {
    return this.getString(id)
}
