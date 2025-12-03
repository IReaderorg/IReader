package ireader.presentation.imageloader

import okio.Path

actual fun Path.setLastModified(epoch: Long) {
    // On iOS, setting file modification date is complex and not critical for cache invalidation
    // The cache will be invalidated when the file is deleted/recreated
    // This is a no-op implementation
}
