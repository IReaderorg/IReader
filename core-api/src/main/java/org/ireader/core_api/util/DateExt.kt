/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.util

import android.text.format.DateUtils
import kotlinx.datetime.*

fun LocalDate.asRelativeTimeString(): String {
  return DateUtils
    .getRelativeTimeSpanString(
      atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
      System.currentTimeMillis(),
      DateUtils.DAY_IN_MILLIS
    )
    .toString()
}
 class DateTimeFormatter  constructor(pattern: String) {
  internal val jtFormatter = java.time.format.DateTimeFormatter.ofPattern(pattern)
}

 fun LocalDateTime.format(formatter: DateTimeFormatter): String {
  return toJavaLocalDateTime().format(formatter.jtFormatter)
}
