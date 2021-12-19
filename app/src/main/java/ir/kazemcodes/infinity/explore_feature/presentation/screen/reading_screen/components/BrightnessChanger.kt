package ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.components

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity


fun Context.getActivity(): AppCompatActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is AppCompatActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}