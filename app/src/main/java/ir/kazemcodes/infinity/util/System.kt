package ir.kazemcodes.infinity.util

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun showInterstitial(context: Context) {
    val activity = context.findActivity()
}