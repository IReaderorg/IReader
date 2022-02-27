/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

// These should be removed when we have formatters in kotlin-datetime

expect fun LocalDate.asRelativeTimeString(): String

expect class DateTimeFormatter(pattern: String)

expect fun LocalDateTime.format(formatter: DateTimeFormatter): String
