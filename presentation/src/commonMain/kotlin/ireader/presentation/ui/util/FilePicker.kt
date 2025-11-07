package ireader.presentation.ui.util

expect object FilePicker {
    fun pickFileForSave(
        title: String = "Save File",
        defaultFileName: String = "glossary.json",
        onFileSelected: (String, ByteArray) -> Unit,
        onCancelled: () -> Unit = {}
    )
    
    fun pickFileForLoad(
        title: String = "Open File",
        onFileSelected: (String, ByteArray) -> Unit,
        onCancelled: () -> Unit = {}
    )
}
