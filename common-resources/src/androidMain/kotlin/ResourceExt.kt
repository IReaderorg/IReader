package ireader.common.resources

import android.content.Context

fun Context.string(id: Int): String {
    return this.getString(id)
}