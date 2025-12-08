package ireader.domain.services.platform

/**
 * Aggregate for platform-specific services.
 * Groups clipboard, share, file system, haptic, and network services.
 * 
 * Usage:
 * ```kotlin
 * class LibraryViewModel(
 *     private val platformServices: PlatformServices,
 *     // ... other deps
 * ) {
 *     fun copyToClipboard(text: String) {
 *         platformServices.clipboard.copyText(text)
 *     }
 *     
 *     fun shareBook(uri: Uri) {
 *         platformServices.share.shareFile(uri, "application/epub+zip")
 *     }
 * }
 * ```
 * 
 * Requirements: 4.3 - PlatformServices aggregate groups platform services
 */
data class PlatformServices(
    val clipboard: ClipboardService,
    val share: ShareService,
    val fileSystem: FileSystemService,
    val haptic: HapticService,
    val network: NetworkService
)
