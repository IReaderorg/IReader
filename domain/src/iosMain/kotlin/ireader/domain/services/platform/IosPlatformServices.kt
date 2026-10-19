package ireader.domain.services.platform

import ireader.domain.models.common.Uri
import ireader.domain.services.common.ServiceResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.UIKit.UIPasteboard

class IosClipboardService : ClipboardService {
    private var running = false

    override suspend fun initialize() = Unit
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() = Unit

    override suspend fun copyText(text: String, label: String?): ServiceResult<Unit> {
        UIPasteboard.generalPasteboard.string = text
        return ServiceResult.Success(Unit)
    }

    override suspend fun getText(): ServiceResult<String> {
        val text = UIPasteboard.generalPasteboard.string
        return if (text != null) ServiceResult.Success(text) else ServiceResult.Error("Clipboard is empty")
    }

    override suspend fun hasText(): Boolean = UIPasteboard.generalPasteboard.hasStrings

    override suspend fun clear(): ServiceResult<Unit> {
        UIPasteboard.generalPasteboard.string = null
        return ServiceResult.Success(Unit)
    }
}

class IosShareService : ShareService {
    private var running = false

    override suspend fun initialize() = Unit
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() = Unit

    override suspend fun shareText(text: String, title: String?) = unsupported()
    override suspend fun shareFile(uri: Uri, mimeType: String, title: String?) = unsupported()
    override suspend fun shareFiles(uris: List<Uri>, mimeType: String, title: String?) = unsupported()
    override suspend fun shareUrl(url: String, title: String?) = unsupported()
    override fun isSharingSupported(): Boolean = false

    private fun unsupported() = ServiceResult.Error("Sharing is not wired on iOS yet")
}

class IosFileSystemService : FileSystemService {
    private val files = FileSystem.SYSTEM
    private var running = false

    override suspend fun initialize() = Unit
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() = Unit

    override suspend fun pickFile(initialDirectory: String?, fileTypes: List<String>, title: String?) =
        ServiceResult.Error("File picker is not wired on iOS yet")

    override suspend fun pickMultipleFiles(initialDirectory: String?, fileTypes: List<String>, title: String?) =
        ServiceResult.Error("File picker is not wired on iOS yet")

    override suspend fun pickDirectory(initialDirectory: String?, title: String?) =
        ServiceResult.Error("Folder picker is not wired on iOS yet")

    override suspend fun saveFile(
        defaultFileName: String,
        fileExtension: String,
        initialDirectory: String?,
        title: String?,
    ) = ServiceResult.Error("Save dialog is not wired on iOS yet")

    override suspend fun fileExists(uri: Uri): Boolean = files.exists(uri.path.toPath())

    override suspend fun getFileSize(uri: Uri): Long? = files.metadataOrNull(uri.path.toPath())?.size

    override suspend fun deleteFile(uri: Uri): ServiceResult<Unit> {
        return runCatching {
            files.delete(uri.path.toPath())
            ServiceResult.Success(Unit)
        }.getOrElse { ServiceResult.Error(it.message ?: "Delete failed", it) }
    }

    override suspend fun readFileBytes(uri: Uri): ServiceResult<ByteArray> {
        return runCatching {
            ServiceResult.Success(files.read(uri.path.toPath()) { readByteArray() })
        }.getOrElse { ServiceResult.Error(it.message ?: "Read failed", it) }
    }

    override suspend fun writeFileBytes(uri: Uri, bytes: ByteArray): ServiceResult<Unit> {
        return runCatching {
            val path = uri.path.toPath()
            path.parent?.let { files.createDirectories(it) }
            files.write(path) { write(bytes) }
            ServiceResult.Success(Unit)
        }.getOrElse { ServiceResult.Error(it.message ?: "Write failed", it) }
    }
}

class IosNetworkService : NetworkService {
    private var running = false
    private val state = NetworkState(
        isConnected = true,
        type = NetworkType.WIFI,
        isMetered = false,
    )

    override suspend fun initialize() = Unit
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() = Unit

    override fun isConnected(): Boolean = true
    override fun isWiFi(): Boolean = true
    override fun isMobile(): Boolean = false
    override fun isEthernet(): Boolean = false
    override fun isMetered(): Boolean = false
    override fun getNetworkState(): NetworkState = state
    override fun observeNetworkChanges(): Flow<NetworkState> = flowOf(state)

    override suspend fun measureNetworkSpeed(): ServiceResult<NetworkSpeed> =
        ServiceResult.Error("Network speed is not measured on iOS yet")

    override suspend fun isHostReachable(host: String, timeoutMs: Long): Boolean = true
}
