package ireader.domain.usecases.backup

/**
 * iOS implementation uses stub (Google Drive not yet implemented for iOS)
 */
actual fun createGoogleDriveProvider(): CloudStorageProvider = GoogleDriveProviderStub()
