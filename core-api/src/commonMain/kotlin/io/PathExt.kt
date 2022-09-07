/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.api.io

import okio.Path

val Path.nameWithoutExtension
  get() = name.substringBeforeLast(".")

val Path.extension
  get() = name.substringAfterLast(".")

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun Path.setLastModified(epoch: Long)
