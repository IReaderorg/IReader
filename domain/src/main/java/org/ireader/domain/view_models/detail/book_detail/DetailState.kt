package org.ireader.domain.view_models.detail.book_detail

import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.source.Source
import org.ireader.domain.source.SourceTower

data class DetailState(
    val source: Source = SourceTower.create(),
    val book: Book = Book(title = "", link = "", sourceId = 0L),
    val inLibrary: Boolean = false,
    val isLocalLoading: Boolean = false,
    val isLocalLoaded: Boolean = false,
    val error: UiText = UiText.DynamicString(""),
    val isRemoteLoaded: Boolean = false,
    val isRemoteLoading: Boolean = false,
)