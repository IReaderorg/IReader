/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.io

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.bits.Memory
import io.ktor.utils.io.bits.of
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual suspend fun ByteReadChannel.peek(bytes: Int, buffer: ByteArray): ByteArray {
    val memory = Memory.of(buffer, 0, bytes)
    peekTo(memory, 0, 0, bytes.toLong(), bytes.toLong())
    return buffer
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
