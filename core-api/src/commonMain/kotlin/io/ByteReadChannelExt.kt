/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.api.io

import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use
import java.io.File

private const val BUFFER_SIZE = 8096

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect suspend fun ByteReadChannel.peek(bytes: Int, buffer: ByteArray = ByteArray(bytes)): ByteArray

suspend fun ByteReadChannel.saveTo(path: Path, fileSystem: FileSystem) {
  withContext(Dispatchers.IO) {
    val buffer = ByteArray(BUFFER_SIZE)
    fileSystem.sink(path).buffer().use { sink ->
      do {
        val read = readAvailable(buffer, 0, BUFFER_SIZE)
        if (read > 0) {
          sink.write(buffer, 0, read)
        }
      } while (read >= 0)
    }
  }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun ByteReadChannel.saveTo(file: File) {
  withContext(Dispatchers.IO) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    file.outputStream().use { output ->
      do {
        val read = readAvailable(buffer, 0, DEFAULT_BUFFER_SIZE)
        if (read > 0) {
          output.write(buffer, 0, read)
        }
      } while (read >= 0)
    }
  }
}
