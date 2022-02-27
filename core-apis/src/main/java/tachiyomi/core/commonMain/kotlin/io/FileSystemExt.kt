/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.io

import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect suspend fun FileSystem.withAsyncSink(path: Path, block: (BufferedSink) -> Unit)

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect suspend fun FileSystem.withAsyncGzipSink(path: Path, block: (BufferedSink) -> Unit)

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect suspend fun <T> FileSystem.withAsyncSource(path: Path, block: (BufferedSource) -> T): T

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect suspend fun <T> FileSystem.withAsyncGzipSource(path: Path, block: (BufferedSource) -> T): T
