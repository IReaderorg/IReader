package ireader.domain.usecases.backup.backup.legecy.twnetysep

import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Page
import ireader.core.source.model.encode
import ireader.domain.usecases.backup.backup.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
private data class LegacyChapterProto(
    @ProtoNumber(1) val key: String,
    @ProtoNumber(2) val name: String,
    @ProtoNumber(3) val translator: String = "",
    @ProtoNumber(4) val read: Boolean = false,
    @ProtoNumber(5) val bookmark: Boolean = false,
    @ProtoNumber(6) val dateFetch: Long = 0,
    @ProtoNumber(7) val dateUpload: Long = 0,
    @ProtoNumber(8) val number: Float = 0f,
    @ProtoNumber(9) val sourceOrder: Int = 0,
    @ProtoNumber(10) val content: List<Page> = emptyList(),
) {

    fun toChapterProto(): ChapterProto {
        return ChapterProto(
            key = key,
            name = name,
            translator = translator,
            read = read,
            bookmark = bookmark,
            dateFetch = dateFetch,
            dateUpload = dateUpload,
            number = number,
            sourceOrder = sourceOrder.toLong(),
            content = content.encode(),
            type = ChapterInfo.MIX,
            lastPageRead = 0,
        )
    }

}


@Serializable
private data class BackupProto(
    @ProtoNumber(1) val library: List<LegacyBookProto> = emptyList(),
    @ProtoNumber(2) val categories: List<CategoryProto> = emptyList()
) {
    fun toBackup(): Backup {
        return Backup(
            library.map { it.toBookProto() },
            categories
        )
    }
}

@Serializable
private data class LegacyBackupProto @OptIn(ExperimentalSerializationApi::class) constructor(
    @ProtoNumber(1) val library: List<LegacyBookProto> = emptyList(),
    @ProtoNumber(2) val categories: List<CategoryProto> = emptyList()
) {
    fun toBackup(): Backup {
        return Backup(
            library.map { it.toBookProto() },
            categories
        )
    }
}


@OptIn(ExperimentalSerializationApi::class)
internal fun ByteArray.dumpTwentySepLegacyBackup(): Backup {
    return ProtoBuf.decodeFromByteArray<LegacyBackupProto>(
        this
    ).toBackup()
}

@Serializable
private data class LegacyBookProto @OptIn(ExperimentalSerializationApi::class) constructor(
    @ProtoNumber(1) val sourceId: Long,
    @ProtoNumber(2) val key: String,
    @ProtoNumber(3) val title: String,
    @ProtoNumber(4) val author: String = "",
    @ProtoNumber(5) val description: String = "",
    @ProtoNumber(6) val genres: List<String> = emptyList(),
    @ProtoNumber(7) val status: Int = 0,
    @ProtoNumber(8) val cover: String = "",
    @ProtoNumber(9) val customCover: String = "",
    @ProtoNumber(10) val lastUpdate: Long = 0,
    @ProtoNumber(11) val lastInit: Long = 0,
    @ProtoNumber(12) val dateAdded: Long = 0,
    @ProtoNumber(13) val viewer: Int = 0,
    @ProtoNumber(14) val flags: Int = 0,
    @ProtoNumber(15) val chapters: List<LegacyChapterProto> = emptyList(),
    @ProtoNumber(16) val categories: List<Int> = emptyList(),
    @ProtoNumber(17) val tracks: List<TrackProto> = emptyList(),
    @ProtoNumber(18) val histories: List<HistoryProto> = emptyList(),
) {
    fun toBookProto(): BookProto {
        return BookProto(
            sourceId = sourceId,
            key = key,
            title = title,
            author = author,
            description = description,
            genres = genres,
            status = status.toLong(),
            cover = cover,
            customCover = customCover,
            lastUpdate = lastUpdate,
            viewer = viewer.toLong(),
            flags = flags.toLong(),
            dateAdded = dateAdded,
            categories = categories.map { it.toLong() },
            chapters = chapters.map {
                it.toChapterProto()
            },
            histories = histories,
            tracks = tracks
        )
    }


}
