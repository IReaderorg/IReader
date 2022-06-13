package org.ireader.domain.use_cases.reader

import android.content.Context
import android.view.WindowManager
import org.ireader.common_extensions.findComponentActivity
import javax.inject.Inject

class ScreenAlwaysOn @Inject constructor() {
    operator fun invoke(context: Context, enable: Boolean) {
        when (enable) {
            true -> {
                context.findComponentActivity()?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            false -> {
                context.findComponentActivity()?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}