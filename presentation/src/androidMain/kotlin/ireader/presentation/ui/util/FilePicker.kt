package ireader.presentation.ui.util

// Android implementation would use Activity Result APIs
// For now, provide a stub implementation
actual object FilePicker {
    actual fun pickFileForSave(
        title: String,
        defaultFileName: String,
        onFileSelected: (String, ByteArray) -> Unit,
        onCancelled: () -> Unit
    ) {
        // TODO: Implement Android file picker using Activity Result APIs
        onCancelled()
    }
    
    actual fun pickFileForLoad(
        title: String,
        onFileSelected: (String, ByteArray) -> Unit,
        onCancelled: () -> Unit
    ) {
        // TODO: Implement Android file picker using Activity Result APIs
        onCancelled()
    }
}
