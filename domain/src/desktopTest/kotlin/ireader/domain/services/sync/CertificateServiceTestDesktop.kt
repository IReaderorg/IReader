package ireader.domain.services.sync

import ireader.data.sync.encryption.DesktopCertificateService

/**
 * Desktop-specific factory for CertificateService tests.
 * 
 * Uses DesktopCertificateService.
 */
actual fun createCertificateService(): CertificateService {
    return DesktopCertificateService()
}
