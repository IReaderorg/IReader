package org.ireader.core.okhttp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.*
import java.io.File
import kotlin.io.use

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun Source.saveTo(path: Path, fileSystem: FileSystem) {
    withContext(Dispatchers.IO) {
        use { source ->
            fileSystem.sink(path).buffer().use { it.writeAll(source) }
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun Source.saveTo(file: File) {
    withContext(Dispatchers.IO) {
        use { source ->
            file.sink().buffer().use { it.writeAll(source) }
        }
    }
}