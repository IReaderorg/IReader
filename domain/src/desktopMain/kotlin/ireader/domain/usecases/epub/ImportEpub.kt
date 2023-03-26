package ireader.domain.usecases.epub

import ireader.domain.usecases.files.GetSimpleStorage

actual class ImportEpub(
    private val getSimpleStorage: GetSimpleStorage
) {
    actual suspend fun parse(uris: List<ireader.domain.models.common.Uri>) {
    }

    actual fun getCacheSize(): String {
        return getSimpleStorage.getCacheSize()
    }

    actual fun removeCache() {
        return getSimpleStorage.clearCache()
    }


}