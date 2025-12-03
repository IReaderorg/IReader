package ireader.presentation.imageloader

import okio.Path

actual fun Path.setLastModified(epoch: Long) {
    toFile().setLastModified(epoch)
}
