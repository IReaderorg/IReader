package org.ireader.domain.models.source

data class ChapterInfo(
    var key: String,
    var name: String,
    var dateUpload: Long = 0,
    var number: Float = -1f,
    var translator: String = "",
)