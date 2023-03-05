package ireader.domain.usecases.reader

import android.content.Context
import android.view.WindowManager
import ireader.domain.utils.extensions.findComponentActivity


class ScreenAlwaysOnImpl(private val context: Context) : ScreenAlwaysOn {
    override operator fun invoke(enable: Boolean) {
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
