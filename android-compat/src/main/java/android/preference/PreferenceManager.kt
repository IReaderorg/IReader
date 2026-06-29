package android.preference

import android.content.Context

/**
 * Created by nulldev on 3/26/17.
 */

class PreferenceManager {
    companion object {
        @JvmStatic
        fun getDefaultSharedPreferences(context: Context) =
            context.getSharedPreferences(
                context.getPackageName() ?: "",
                Context.MODE_PRIVATE,
            )!!
    }
}
