package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri
import ireader.domain.usecases.files.GetSimpleStorage

actual class ImportEpub(
    private val getSimpleStorage: GetSimpleStorage
) {
    actual suspend fun parse(uri: Uri) {
    }

    actual fun getCacheSize(): String {
        return getSimpleStorage.getCacheSize()
    }

    actual fun removeCache() {
        return getSimpleStorage.clearCache()
    }


}