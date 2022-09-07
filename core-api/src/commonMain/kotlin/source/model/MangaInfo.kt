

package ireader.core.api.source.model

/**
 * Model for a manga given by a source
 *
 */
data class MangaInfo(
    val key: String,
    val title: String,
    val artist: String = "",
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Int = UNKNOWN,
    val cover: String = ""
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
