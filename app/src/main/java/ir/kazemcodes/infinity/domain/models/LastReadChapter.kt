package ir.kazemcodes.infinity.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LastReadChapter(
    val bookName : String,
    val source : String,
    val chapterLink : String,
    val chapterTitle : String,
) : Parcelable

