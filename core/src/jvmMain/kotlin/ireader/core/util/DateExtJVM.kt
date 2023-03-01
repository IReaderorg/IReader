/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime

actual class DateTimeFormatter actual constructor(pattern: String) {
  internal val jtFormatter = java.time.format.DateTimeFormatter.ofPattern(pattern)
}

actual fun LocalDateTime.format(formatter: DateTimeFormatter): String {
  return toJavaLocalDateTime().format(formatter.jtFormatter)
}
