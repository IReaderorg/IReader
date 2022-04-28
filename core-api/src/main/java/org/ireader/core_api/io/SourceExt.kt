

package org.ireader.core_api.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Source
import okio.buffer
import okio.sink
import java.io.File

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun Source.saveTo(path: Path, fileSystem: FileSystem) {
    withContext(Dispatchers.IO) {
        use { source ->
            fileSystem.sink(path).buffer().use { it.writeAll(source) }
        }
    }
}

suspend fun Source.saveTo(file: File) {
    withContext(Dispatchers.IO) {
        use { source ->
            file.sink().buffer().use { it.writeAll(source) }
        }
    }
}
