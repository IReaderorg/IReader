package org.ireader.domain.models.source

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ireader.core.utils.Constants
import org.ireader.domain.source.SourceTower


@Serializable
@Entity(tableName = Constants.SOURCE_TABLE)
data class SourceEntity(
    var baseUrl: String,
    var lang: String,
    var name: String,
    var creator: String,
    var creatorNote: String? = null,
    var supportsMostPopular: Boolean = false,
    var supportSearch: Boolean = false,
    var supportsLatest: Boolean = false,
    var imageIcon: String = "",
    var dateAdded: Long = 0,
    var dateChanged: Long = 0,
    var customSource: Boolean = false,
    @Embedded
    var latest: Latest? = null,
    @Embedded
    var popular: Popular? = null,
    @Embedded
    var detail: Detail? = null,
    @Embedded
    var search: Search? = null,
    @Embedded
    var chapters: Chapters? = null,
    @Embedded
    var content: Content? = null,
    @PrimaryKey(autoGenerate = true) val sourceId: Long = 0,
) {
    fun toSource(): SourceTower {
        return SourceTower(
            sourceId = sourceId,
            baseUrl = baseUrl,
            lang = lang,
            name = name,
            creator = creator,
            supportsMostPopular = supportsMostPopular,
            supportSearch = supportSearch,
            supportsLatest = supportsLatest,
            latest = latest,
            popular = popular,
            detail = detail,
            search = search,
            chapters = chapters,
            content = content,
            creatorNote = creatorNote
        )
    }
}