

package ireader.domain.usecases.backup.backup
import ireader.domain.models.entities.Book
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
internal data class BookProto(
    @ProtoNumber(1) val sourceId: Long,
    @ProtoNumber(2) val key: String,
    @ProtoNumber(3) val title: String,
    @ProtoNumber(4) val author: String = "",
    @ProtoNumber(5) val description: String = "",
    @ProtoNumber(6) val genres: List<String> = emptyList(),
    @ProtoNumber(7) val status: Long = 0,
    @ProtoNumber(8) val cover: String = "",
    @ProtoNumber(9) val customCover: String = "",
    @ProtoNumber(10) val lastUpdate: Long = 0,
    @ProtoNumber(11) val initialized: Boolean = false,
    @ProtoNumber(12) val dateAdded: Long = 0,
    @ProtoNumber(13) val viewer: Long = 0,
    @ProtoNumber(14) val flags: Long = 0,
    @ProtoNumber(15) val chapters: List<ChapterProto> = emptyList(),
    @ProtoNumber(16) val categories: List<Long> = emptyList(),
    @ProtoNumber(17) val tracks: List<TrackProto> = emptyList(),
    @ProtoNumber(18) val histories: List<HistoryProto> = emptyList(),
) {

    fun toDomain(): Book {
        return Book(
            sourceId = sourceId,
            key = key,
            title = title,
            author = author,
            description = description,
            genres = genres,
            status = status,
            cover = cover,
            customCover = customCover,
            favorite = true, // If present in backup this is a favorite
            lastUpdate = lastUpdate,
            initialized = initialized,
            viewer = viewer,
            flags = flags,
            dateAdded = dateAdded,
        )
    }

    companion object {
        fun fromDomain(
            manga: Book,
            chapters: List<ChapterProto> = emptyList(),
            categories: List<Long> = emptyList(),
            tracks: List<TrackProto> = emptyList(),
            histories: List<HistoryProto> = emptyList(),
        ): BookProto {
            return BookProto(
                sourceId = manga.sourceId,
                key = manga.key,
                title = manga.title,
                author = manga.author,
                description = manga.description,
                genres = manga.genres,
                status = manga.status,
                cover = manga.cover,
                customCover = manga.customCover,
                lastUpdate = manga.lastUpdate,
                initialized = manga.initialized,
                dateAdded = manga.dateAdded,
                viewer = manga.viewer,
                flags = manga.flags,
                chapters = chapters,
                categories = categories,
                tracks = tracks,
                histories = histories,

            )
        }
    }
}

