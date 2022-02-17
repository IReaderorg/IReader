package org.ireader.source.models

data class BookInfo(
    val link: String,
    val title: String,
    val translator: String = "",
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Int = UNKNOWN,
    val cover: String = "",
    val viewer: Int = 0,
    val rating: Int = 0,
) {

  companion object {

      const val UNKNOWN = 0
    const val ONGOING = 1
    const val COMPLETED = 2
    const val LICENSED = 3
    const val PUBLISHING_FINISHED = 4
    const val CANCELLED = 5
  }
}
