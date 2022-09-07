/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.api.io

import okio.Buffer
import okio.Source
import okio.Timeout
import ireader.core.api.util.decodeBase64

class DataUriStringSource(private val data: String) : Source {

  private val timeout = Timeout.NONE

  private val headers = data.substringBefore(",")

  private var pos = headers.length + 1

  private val decoder: (Buffer, String) -> Long = if ("base64" in headers) {
    { sink, bytes ->
      val decoded = bytes.decodeBase64()
      sink.write(decoded)
      decoded.size.toLong()
    }
  } else {
    { sink, bytes ->
      val decoded = bytes.encodeToByteArray()
      sink.write(decoded)
      decoded.size.toLong()
    }
  }

  override fun read(sink: Buffer, byteCount: Long): Long {
    if (pos >= data.length) return -1

    val charsToRead = minOf(data.length - pos, byteCount.toInt())
    val nextChars = data.substring(pos, pos + charsToRead)

    pos += charsToRead

    return decoder(sink, nextChars)
  }

  override fun timeout(): Timeout {
    return timeout
  }

  override fun close() {
  }

}
