package ir.kazemcodes.infinity.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LastReadChapter(
    val book: Book,
    val latestReadChapter : Chapter?=null
) : Parcelable

