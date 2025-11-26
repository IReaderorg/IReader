package ireader.presentation.core.util

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri


/**
 * @deprecated Use FileSystemService from domain layer instead
 * 
 * This composable is deprecated and will be removed in a future release.
 * Use FileSystemService in your ViewModel for file picking operations.
 * 
 * Migration example:
 * ```
 * // Before
 * @Composable
 * fun MyScreen() {
 *     var showPicker by remember { mutableStateOf(false) }
 *     FilePicker(show = showPicker, onFileSelected = { uri -> ... })
 * }
 * 
 * // After
 * class MyViewModel(
 *     private val fileSystemService: FileSystemService
 * ) {
 *     fun pickFile() {
 *         scope.launch {
 *             fileSystemService.pickFile(fileTypes = listOf("epub"))
 *                 .onSuccess { uri -> handleFile(uri) }
 *         }
 *     }
 * }
 * 
 * @Composable
 * fun MyScreen(viewModel: MyViewModel) {
 *     Button(onClick = { viewModel.pickFile() }) { Text("Pick File") }
 * }
 * ```
 */
@Deprecated(
    message = "Use FileSystemService from domain layer instead",
    replaceWith = ReplaceWith(
        "fileSystemService.pickFile(fileTypes = fileExtensions)",
        "ireader.domain.services.platform.FileSystemService"
    ),
    level = DeprecationLevel.WARNING
)
@Composable
expect fun FilePicker(
	show: Boolean,
	initialDirectory: String? = null,
	fileExtensions: List<String> = emptyList(),
	onFileSelected: (Uri?) -> Unit
)

/**
 * @deprecated Use FileSystemService from domain layer instead
 * 
 * This composable is deprecated and will be removed in a future release.
 * Use FileSystemService.pickDirectory() in your ViewModel instead.
 * 
 * Migration example:
 * ```
 * // Before
 * DirectoryPicker(show = true, onFileSelected = { path -> ... })
 * 
 * // After
 * class MyViewModel(private val fileSystemService: FileSystemService) {
 *     fun pickDirectory() {
 *         scope.launch {
 *             fileSystemService.pickDirectory()
 *                 .onSuccess { uri -> handleDirectory(uri) }
 *         }
 *     }
 * }
 * ```
 */
@Deprecated(
    message = "Use FileSystemService from domain layer instead",
    replaceWith = ReplaceWith(
        "fileSystemService.pickDirectory()",
        "ireader.domain.services.platform.FileSystemService"
    ),
    level = DeprecationLevel.WARNING
)
@Composable
expect fun DirectoryPicker(
    show: Boolean,
    initialDirectory: String? = null,
    onFileSelected: (String?) -> Unit
)