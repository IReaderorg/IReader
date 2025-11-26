package ireader.presentation.core

/**
 * @deprecated Use ClipboardService from domain layer instead
 * 
 * This class is deprecated and will be removed in a future release.
 * Use ClipboardService for clipboard operations.
 * 
 * Migration example:
 * ```
 * // Before
 * val platformHelper: PlatformHelper
 * platformHelper.copyToClipboard("label", "text")
 * 
 * // After
 * val clipboardService: ClipboardService
 * scope.launch {
 *     clipboardService.copyText("text", "label")
 * }
 * ```
 */
@Deprecated(
    message = "Use ClipboardService from domain layer instead",
    replaceWith = ReplaceWith(
        "clipboardService.copyText(content, label)",
        "ireader.domain.services.platform.ClipboardService"
    ),
    level = DeprecationLevel.WARNING
)
expect class PlatformHelper {
     fun copyToClipboard(label: String, content: String)
}