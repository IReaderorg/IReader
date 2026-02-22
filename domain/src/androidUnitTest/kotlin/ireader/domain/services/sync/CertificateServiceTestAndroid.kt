package ireader.domain.services.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ireader.data.sync.encryption.AndroidCertificateService

/**
 * Android-specific factory for CertificateService tests.
 * 
 * Uses AndroidCertificateService with test context.
 */
actual fun createCertificateService(): CertificateService {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return AndroidCertificateService(context)
}
