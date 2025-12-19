package ireader.plugin.api.source

import kotlinx.serialization.Serializable

/**
 * Events emitted by source loaders for UI updates.
 * The main app can observe these to update UI accordingly.
 */
@Serializable
sealed class SourceEvent {
    /** A new source was loaded */
    @Serializable
    data class SourceLoaded(val source: SourceInfo) : SourceEvent()
    
    /** A source was unloaded */
    @Serializable
    data class SourceUnloaded(val sourceId: Long) : SourceEvent()
    
    /** An extension was installed */
    @Serializable
    data class ExtensionInstalled(val extension: SourceExtensionInfo) : SourceEvent()
    
    /** An extension was uninstalled */
    @Serializable
    data class ExtensionUninstalled(val extensionId: String) : SourceEvent()
    
    /** An extension was updated */
    @Serializable
    data class ExtensionUpdated(val extension: SourceExtensionInfo) : SourceEvent()
    
    /** Extension list was refreshed from repositories */
    @Serializable
    data class RepositoryRefreshed(val repoUrl: String, val extensionCount: Int) : SourceEvent()
    
    /** Error occurred */
    @Serializable
    data class Error(val message: String, val sourceId: Long? = null) : SourceEvent()
}

/**
 * Basic source info for events (lighter than full UnifiedSource).
 */
@Serializable
data class SourceInfo(
    val id: Long,
    val name: String,
    val lang: String,
    val iconUrl: String?,
    val loaderType: SourceLoaderType,
    val contentType: SourceContentType,
    val isNsfw: Boolean = false
)

/**
 * Source loading state.
 */
@Serializable
sealed class SourceLoadingState {
    @Serializable
    object Idle : SourceLoadingState()
    
    @Serializable
    data class Loading(val message: String = "") : SourceLoadingState()
    
    @Serializable
    data class Progress(val current: Int, val total: Int, val message: String = "") : SourceLoadingState()
    
    @Serializable
    data class Success(val message: String = "") : SourceLoadingState()
    
    @Serializable
    data class Error(val message: String, val cause: String? = null) : SourceLoadingState()
}

/**
 * Download state for extensions.
 */
@Serializable
sealed class ExtensionDownloadState {
    @Serializable
    object Idle : ExtensionDownloadState()
    
    @Serializable
    data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : ExtensionDownloadState()
    
    @Serializable
    object Installing : ExtensionDownloadState()
    
    @Serializable
    object Completed : ExtensionDownloadState()
    
    @Serializable
    data class Failed(val error: String) : ExtensionDownloadState()
}
