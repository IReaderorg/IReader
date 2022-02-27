package org.ireader.domain.view_models.detail.book_detail

import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import tachiyomi.source.CatalogSource

data class DetailState(
    val source: CatalogSource? = null,
    val book: Book? = null,
    val inLibrary: Boolean = false,
    val isLocalLoading: Boolean = false,
    val isRemoteLoading: Boolean = false,
    val isLocalLoaded: Boolean = false,
    val error: UiText = UiText.DynamicString(""),
    val isRemoteLoaded: Boolean = false,
)