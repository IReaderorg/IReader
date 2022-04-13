package org.ireader.domain.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class LastReadChapter(
    val bookName: String,
    val source: String,
    val chapterLink: String,
    val chapterTitle: String,
)

