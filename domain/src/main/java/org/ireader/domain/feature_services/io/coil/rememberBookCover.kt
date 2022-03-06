/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.domain.feature_services.io.coil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.ireader.domain.feature_services.io.BookCover
import org.ireader.domain.models.entities.Book

@Composable
fun rememberBookCover(manga: Book): BookCover {
  return remember(manga.id) {
    BookCover.from(manga)
  }
}
