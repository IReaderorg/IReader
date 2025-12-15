package ireader.presentation.core.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * This code is derived from the terrific PACMC project:
 * https://github.com/jakobkmar/pacmc
 */
internal object FileChooser {
    private enum class CallType {
        FILE,
        DIRECTORY
    }

    suspend fun chooseFile(
        initialDirectory: String = System.getProperty("user.dir"),
        fileExtensions: String = ""
    ): String? {
        return chooseFile(CallType.FILE, initialDirectory, fileExtensions)
    }

    suspend fun chooseDirectory(
        initialDirectory: String = System.getProperty("user.dir"),
    ): String? {
        return chooseFile(CallType.DIRECTORY, initialDirectory)
    }

    suspend fun saveFile(
        initialDirectory: String = System.getProperty("user.dir"),
        defaultFileName: String = "",
        fileExtensions: String = ""
    ): String? {
        return kotlin.runCatching { saveFileNative(initialDirectory, defaultFileName, fileExtensions) }
            .onFailure {
                return kotlin.runCatching { saveFileSwing(initialDirectory, defaultFileName, fileExtensions) }
                    .getOrNull()
            }
            .getOrNull()
    }

    private suspend fun saveFileNative(
        initialDirectory: String,
        defaultFileName: String,
        fileExtension: String
    ) = withContext(Dispatchers.IO) {
        val pathPointer = MemoryUtil.memAllocPointer(1)
        try {
            return@withContext when (val code = NativeFileDialog.NFD_SaveDialog(fileExtension, initialDirectory, pathPointer)) {
                NativeFileDialog.NFD_OKAY -> {
                    val path = pathPointer.stringUTF8
                    NativeFileDialog.nNFD_Free(pathPointer[0])
                    path
                }
                NativeFileDialog.NFD_CANCEL -> null
                NativeFileDialog.NFD_ERROR -> error("An error occurred while executing NativeFileDialog.NFD_SaveDialog")
                else -> error("Unknown return code '${code}' from NativeFileDialog.NFD_SaveDialog")
            }
        } finally {
            MemoryUtil.memFree(pathPointer)
        }
    }

    private suspend fun saveFileSwing(
        initialDirectory: String,
        defaultFileName: String,
        fileExtension: String
    ) = withContext(Dispatchers.IO) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        val chooser = JFileChooser(initialDirectory).apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            selectedFile = java.io.File(initialDirectory, defaultFileName)
            addChoosableFileFilter(FileNameExtensionFilter(fileExtension, fileExtension))
            isVisible = true
        }

        when (val code = chooser.showSaveDialog(null)) {
            JFileChooser.APPROVE_OPTION -> chooser.selectedFile.absolutePath
            JFileChooser.CANCEL_OPTION -> null
            JFileChooser.ERROR_OPTION -> error("An error occurred while executing JFileChooser::showSaveDialog")
            else -> error("Unknown return code '${code}' from JFileChooser::showSaveDialog")
        }
    }

    private suspend fun chooseFile(
        type: CallType,
        initialDirectory: String,
        fileExtensions: String = ""
    ): String? {
        return kotlin.runCatching { chooseFileNative(type, initialDirectory, fileExtensions) }
            .onFailure {
                return kotlin.runCatching { chooseFileSwing(type, initialDirectory, fileExtensions) }
                    .getOrNull()
            }
            .getOrNull()
    }

    private suspend fun chooseFileNative(
        type: CallType,
        initialDirectory: String,
        fileExtension: String
    ) = withContext(Dispatchers.IO) {
        val pathPointer = MemoryUtil.memAllocPointer(1)
        try {
            return@withContext when (val code = when (type) {
                CallType.FILE -> NativeFileDialog.NFD_OpenDialog(fileExtension, initialDirectory, pathPointer)
                CallType.DIRECTORY -> NativeFileDialog.NFD_PickFolder(initialDirectory, pathPointer)
            }) {
                NativeFileDialog.NFD_OKAY -> {
                    val path = pathPointer.stringUTF8
                    NativeFileDialog.nNFD_Free(pathPointer[0])

                    path
                }

                NativeFileDialog.NFD_CANCEL -> null
                NativeFileDialog.NFD_ERROR -> error("An error occurred while executing NativeFileDialog.NFD_PickFolder")
                else -> error("Unknown return code '${code}' from NativeFileDialog.NFD_PickFolder")
            }
        } finally {
            MemoryUtil.memFree(pathPointer)
        }
    }

    private suspend fun chooseFileSwing(
        type: CallType,
        initialDirectory: String,
        fileExtension: String
    ) = withContext(Dispatchers.IO) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        val chooser = when (type) {
            CallType.FILE -> {
                JFileChooser(initialDirectory).apply {
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    acceptAllFileFilter
                    isVisible = true
                    addChoosableFileFilter(FileNameExtensionFilter(fileExtension, fileExtension))
                }
            }

            CallType.DIRECTORY -> {
                JFileChooser(initialDirectory).apply {
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    isVisible = true
                }
            }
        }

        when (val code = chooser.showOpenDialog(null)) {
            JFileChooser.APPROVE_OPTION -> chooser.selectedFile.absolutePath
            JFileChooser.CANCEL_OPTION -> null
            JFileChooser.ERROR_OPTION -> error("An error occurred while executing JFileChooser::showOpenDialog")
            else -> error("Unknown return code '${code}' from JFileChooser::showOpenDialog")
        }
    }
}