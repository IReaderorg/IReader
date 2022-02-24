package org.ireader.source.models

data class BookInfo(
    val key: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Int = UNKNOWN,
    val cover: String = "",
) {

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        const val PUBLISHING_FINISHED = 4
        const val CANCELLED = 5
        const val ON_HIATUS = 6
    }
}