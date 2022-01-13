package ir.kazemcodes.infinity.util

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

fun Context.findAppCompatAcivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.findAppCompatAcivity()
    else -> null
}

fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}


fun showInterstitial(context: Context) {
    val activity = context.findAppCompatAcivity()
}