package ir.kazemcodes.infinity.util

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity

fun Context.findActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
fun showInterstitial(context: Context) {
    val activity = context.findActivity()
}