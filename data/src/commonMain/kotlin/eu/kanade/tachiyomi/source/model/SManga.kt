@file:Suppress("PropertyName")

package eu.kanade.tachiyomi.source.model

import kotlinx.serialization.json.JsonObject
import java.io.Serializable

/**
 * Minimal SManga interface shim for tsundoku extension compatibility.
 */
interface SManga : Serializable {
    var url: String
    var title: String
    var thumbnail_url: String?
    var artist: String?
    var author: String?
    var status: Int
    var description: String?
    var genre: String?
    var update_strategy: UpdateStrategy
    var initialized: Boolean
    var altTitles: List<String>
        get() = emptyList()
        set(_) {}
    var memo: JsonObject

    fun getGenres(): List<String>? {
        if (genre.isNullOrBlank()) return null
        return genre?.split(Regex("[,;|]+"))?.map { it.trim() }?.filterNot { it.isBlank() }?.distinct()
    }

    fun copy(): SManga {
        return SMangaImpl().also {
            it.url = url
            it.title = title
            it.artist = artist
            it.author = author
            it.description = description
            it.genre = genre
            it.status = status
            it.thumbnail_url = thumbnail_url
            it.update_strategy = update_strategy
            it.initialized = initialized
        }
    }

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        const val PUBLISHING_FINISHED = 4
        const val CANCELLED = 5
        const val ON_HIATUS = 6

        fun create(): SManga = SMangaImpl()
    }
}

/**
 * Default SManga implementation.
 */
class SMangaImpl : SManga {
    override var url: String = ""
    override var title: String = ""
    override var thumbnail_url: String? = null
    override var artist: String? = null
    override var author: String? = null
    override var status: Int = SManga.UNKNOWN
    override var description: String? = null
    override var genre: String? = null
    override var update_strategy: UpdateStrategy = UpdateStrategy.ALWAYS_UPDATE
    override var initialized: Boolean = false
    override var memo: JsonObject = JsonObject(emptyMap())
}

/**
 * Update strategy enum.
 */
enum class UpdateStrategy {
    ALWAYS_UPDATE,
    ONLY_FETCH_ONCE,
    PREFER_SOURCE
}
