/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.io

import ireader.core.io.ZipWriterScope
import okio.FileSystem
import okio.Path
import okio.Source
import okio.buffer
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

actual fun FileSystem.createZip(
  destination: Path,
  compress: Boolean,
  block: ZipWriterScope.() -> Unit
) {
  try {
    val outStream = ZipOutputStream(destination.toFile().outputStream())
    if (!compress) {
      outStream.setLevel(Deflater.NO_COMPRESSION)
    }

    outStream.use {
      block(ZipWriterScope(outStream))
    }
  } catch (e: Exception) {
    delete(destination)
    throw e
  }
}

actual class ZipWriterScope(
  private val stream: ZipOutputStream
) {

  actual fun addFile(destination: String, source: Source) {
    val entry = ZipEntry(destination)
    stream.putNextEntry(entry)
    source.buffer().use { bufferedSource ->
      bufferedSource.inputStream().use { input ->
        input.copyTo(stream)
      }
    }
    stream.closeEntry()
  }

  actual fun addDirectory(destination: String) {
    val directory = if (destination.endsWith("/")) {
      destination
    } else {
      "$destination/"
    }
    val entry = ZipEntry(directory)
    stream.putNextEntry(entry)
    stream.closeEntry()
  }

}
