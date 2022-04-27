/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_ui.coil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.HistoryWithRelations
import org.ireader.common_models.entities.UpdateWithInfo
import org.ireader.core.io.BookCover

@Composable
fun rememberBookCover(manga: Book): BookCover {
    return remember(manga.id) {
        BookCover.from(manga)
    }
}

@Composable
fun rememberBookCover(history: HistoryWithRelations): BookCover {
    return remember(history.bookId) {
        BookCover.from(history)
    }
}

@Composable
fun rememberBookCover(manga: UpdateWithInfo): BookCover {
    return remember(manga.bookId) {
        BookCover.from(manga)
    }
}
