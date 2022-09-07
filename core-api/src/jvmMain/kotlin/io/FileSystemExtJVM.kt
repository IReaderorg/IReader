/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.api.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.gzip

@Suppress("BlockingMethodInNonBlockingContext")
actual suspend fun FileSystem.withAsyncSink(path: Path, block: (BufferedSink) -> Unit) {
  withContext(Dispatchers.IO) {
    sink(path).buffer().use(block)
  }
}

@Suppress("BlockingMethodInNonBlockingContext")
actual suspend fun FileSystem.withAsyncGzipSink(path: Path, block: (BufferedSink) -> Unit) {
  withContext(Dispatchers.IO) {
    sink(path).gzip().buffer().use(block)
  }
}

@Suppress("BlockingMethodInNonBlockingContext")
actual suspend fun <T> FileSystem.withAsyncSource(path: Path, block: (BufferedSource) -> T): T {
  return withContext(Dispatchers.IO) {
    source(path).buffer().use(block)
  }
}

@Suppress("BlockingMethodInNonBlockingContext")
actual suspend fun <T> FileSystem.withAsyncGzipSource(path: Path, block: (BufferedSource) -> T): T {
  return withContext(Dispatchers.IO) {
    source(path).gzip().buffer().use(block)
  }
}
