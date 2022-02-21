package org.ireader.domain.view_models.detail.book_detail

import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import org.ireader.source.core.Source

data class DetailState(
    val source: Source? = null,
    val book: Book? = null,
    val inLibrary: Boolean = false,
    val isLocalLoading: Boolean = false,
    val isRemoteLoading: Boolean = false,
    val isLocalLoaded: Boolean = false,
    val error: UiText = UiText.DynamicString(""),
    val isRemoteLoaded: Boolean = false,
)