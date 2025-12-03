package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri

/**
 * @deprecated Use FileSystemService from domain layer instead
 * 
 * This composable is deprecated and will be removed in a future release.
 * Use FileSystemService in your ViewModel for backup/restore file operations.
 * 
 * Migration example:
 * ```
 * // Before
 * OnShowRestore(show = true, onFileSelected = { uri -> restore(uri) })
 * 
 * // After
 * class BackupViewModel(private val fileSystemService: FileSystemService) {
 *     fun pickRestoreFile() {
 *         scope.launch {
 *             fileSystemService.pickFile(fileTypes = listOf("gz", "json"))
 *                 .onSuccess { uri -> restore(uri) }
 *         }
 *     }
 * }
 * ```
 */
@Deprecated(
    message = "Use FileSystemService.pickFile() from domain layer instead",
    replaceWith = ReplaceWith(
        "fileSystemService.pickFile(fileTypes = listOf(\"gz\", \"json\"))",
        "ireader.domain.services.platform.FileSystemService"
    ),
    level = DeprecationLevel.WARNING
)
@Composable
expect fun OnShowRestore(show:Boolean, onFileSelected: suspend (Uri) -> Unit)

/**
 * @deprecated Use FileSystemService from domain layer instead
 * 
 * This composable is deprecated and will be removed in a future release.
 * Use FileSystemService.saveFile() in your ViewModel for backup operations.
 * 
 * Migration example:
 * ```
 * // Before
 * OnShowBackup(show = true, onFileSelected = { uri -> createBackup(uri) })
 * 
 * // After
 * class BackupViewModel(private val fileSystemService: FileSystemService) {
 *     fun pickBackupLocation() {
 *         scope.launch {
 *             fileSystemService.saveFile("backup", "gz")
 *                 .onSuccess { uri -> createBackup(uri) }
 *         }
 *     }
 * }
 * ```
 */
@Deprecated(
    message = "Use FileSystemService.saveFile() from domain layer instead",
    replaceWith = ReplaceWith(
        "fileSystemService.saveFile(defaultFileName, \"gz\")",
        "ireader.domain.services.platform.FileSystemService"
    ),
    level = DeprecationLevel.WARNING
)
@Composable
expect fun OnShowBackup(show:Boolean, onFileSelected: suspend (Uri) -> Unit)

/**
 * File picker for LNReader backup import (.zip files)
 * 
 * This composable shows a file picker for selecting LNReader backup files.
 * LNReader backups are ZIP files containing novels, chapters, categories, and settings.
 */
@Composable
expect fun OnShowLNReaderImport(show: Boolean, onFileSelected: suspend (Uri?) -> Unit)
