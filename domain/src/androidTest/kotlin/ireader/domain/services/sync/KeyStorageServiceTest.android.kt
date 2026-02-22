package ireader.domain.services.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

/**
 * Android-specific test factory for KeyStorageService.
 */
@RunWith(AndroidJUnit4::class)
actual fun createKeyStorageService(): KeyStorageService {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return AndroidKeyStorageService(context)
}
