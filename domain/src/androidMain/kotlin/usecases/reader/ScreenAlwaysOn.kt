package ireader.domain.usecases.reader

import android.content.Context
import android.view.WindowManager
import ireader.domain.utils.extensions.findComponentActivity
import ireader.domain.utils.extensions.findComponentActivity



class ScreenAlwaysOn() {
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
