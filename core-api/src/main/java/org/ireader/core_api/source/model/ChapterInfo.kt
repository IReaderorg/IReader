/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.source.model

data class ChapterInfo(
  var key: String,
  var name: String,
  var dateUpload: Long = 0,
  var number: Float = -1f,
  var scanlator: String = ""
)
