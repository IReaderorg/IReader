//package ireader.presentation.core.ui.examples
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import ireader.domain.models.common.Uri
//import ireader.presentation.core.ui.rememberDirectoryPickerLauncher
//import ireader.presentation.core.ui.rememberFilePickerLauncher
//import ireader.presentation.core.ui.rememberMultiFilePickerLauncher
//
///**
// * Example screen demonstrating the unified Platform UI abstraction layer
// */
//@Composable
//fun FilePickerExampleScreen() {
//    var selectedFile by remember { mutableStateOf<Uri?>(null) }
//    var selectedFiles by remember { mutableStateOf<List<Uri>?>(null) }
//    var selectedDirectory by remember { mutableStateOf<String?>(null) }
//
//    // Single file picker
//    val pickSingleFile = rememberFilePickerLauncher(
//        mimeTypes = listOf("pdf", "txt", "epub"),
//        initialDirectory = null
//    ) { uri ->
//        selectedFile = uri
//    }
//
//    // Multiple files picker
//    val pickMultipleFiles = rememberMultiFilePickerLauncher(
//        mimeTypes = listOf("jpg", "png", "gif")
//    ) { uris ->
//        selectedFiles = uris
//    }
//
//    // Directory picker
//    val pickDirectory = rememberDirectoryPickerLauncher { path ->
//        selectedDirectory = path
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        Text("Platform UI Abstraction Examples")
//
//        // Single file picker
//        Button(onClick = pickSingleFile) {
//            Text("Pick Single File (PDF/TXT/EPUB)")
//        }
//        selectedFile?.let {
//            Text("Selected: ${it.path}")
//        }
//
//        // Multiple files picker
//        Button(onClick = pickMultipleFiles) {
//            Text("Pick Multiple Images")
//        }
//        selectedFiles?.let { files ->
//            Text("Selected ${files.size} files")
//            files.forEach { file ->
//                Text("  - ${file.path}", modifier = Modifier.padding(start = 16.dp))
//            }
//        }
//
//        // Directory picker
//        Button(onClick = pickDirectory) {
//            Text("Pick Directory")
//        }
//        selectedDirectory?.let {
//            Text("Selected directory: $it")
//        }
//    }
//}
//
///**
// * Example showing usage in a ViewModel
// */
//class FileImportViewModel {
//    private val filePicker = ireader.presentation.core.ui.getPlatformFilePicker()
//
//    suspend fun importBook(): Result<Uri?> {
//        return filePicker.pickFile(
//            mimeTypes = listOf("epub", "pdf", "txt", "mobi"),
//            initialDirectory = null
//        )
//    }
//
//    suspend fun importMultipleBooks(): Result<List<Uri>?> {
//        return filePicker.pickFiles(
//            mimeTypes = listOf("epub", "pdf", "txt"),
//            initialDirectory = null
//        )
//    }
//
//    suspend fun selectDownloadDirectory(): Result<String?> {
//        return filePicker.pickDirectory()
//    }
//}
