package ir.kazemcodes.infinity.core.domain.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import ir.kazemcodes.infinity.core.utils.moshi
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants
import ir.kazemcodes.infinity.feature_sources.sources.models.*
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = Constants.SOURCE_TABLE)
data class SourceEntity(
    val baseUrl: String,
    val lang: String,
    val name: String,
    val creator: String,
    val supportsMostPopular: Boolean = false,
    val supportSearch: Boolean = false,
    val supportsLatest: Boolean = false,
    @Embedded
    val latest: Latest? = null,
    @Embedded
    val popular: Popular? = null,
    @Embedded
    val detail: Detail? = null,
    @Embedded
    val search: Search? = null,
    @Embedded
    val chapters: Chapters? = null,
    @Embedded
    val content: Content? = null,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
) {
    fun toSource(): SourceTower {
        val moshi: Moshi = moshi
        val jsonAdapter: JsonAdapter<SourceTower> =
            moshi.adapter<SourceTower>(SourceTower::class.java)
        return SourceTower(
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
            )
    }
}