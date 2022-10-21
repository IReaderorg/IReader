package ireader.i18n

import android.content.Context

fun Context.string(id: Int): String {
    return this.getString(id)
}