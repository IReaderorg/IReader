package ireader.presentation.ui.component.utils

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri

/**
 * @deprecated Use FileSystemService from domain layer instead
 * 
 * This composable is deprecated and will be removed in a future release.
 * Use FileSystemService in your ViewModel for file operations instead of
 * managing activity results directly.
 * 
 * Migration example:
 * ```
 * // Before
 * val launcher = ActivityResultListener(
 *     onSuccess = { uri -> handleFile(uri) },
 *     onError = { error -> handleError(error) }
 * )
 * launcher.launch(intent)
 * 
 * // After
 * class MyViewModel(private val fileSystemService: FileSystemService) {
 *     fun pickFile() {
 *         scope.launch {
 *             when (val result = fileSystemService.pickFile()) {
 *                 is ServiceResult.Success -> handleFile(result.data)
 *                 is ServiceResult.Error -> handleError(result.message)
 *             }
 *         }
 *     }
 * }
 * ```
 */
@Deprecated(
    message = "Use FileSystemService from domain layer instead",
    replaceWith = ReplaceWith(
        "fileSystemService.pickFile()",
        "ireader.domain.services.platform.FileSystemService"
    ),
    level = DeprecationLevel.WARNING
)
@Composable
expect fun ActivityResultListener(
        onSuccess: suspend (Uri) -> Unit,
        onError: (Throwable) -> Unit,
) : ActivityResultLauncher

/**
 * @deprecated Use FileSystemService from domain layer instead
 */
@Deprecated(
    message = "Use FileSystemService from domain layer instead",
    level = DeprecationLevel.WARNING
)
expect class ActivityResultLauncher(input:Any) {
    fun launch(intent:Any)
}