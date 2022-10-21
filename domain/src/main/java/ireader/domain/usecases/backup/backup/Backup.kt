package ireader.domain.usecases.backup.backup



import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber


@Serializable
internal data class Backup(
    val library: List<BookProto> = emptyList(),
    val categories: List<CategoryProto> = emptyList()
)

internal fun ByteArray.dunmpStableBackup() : Backup {
    return ProtoBuf.decodeFromByteArray<BackupProto>(this).toBackup()
}


@Serializable
internal data class BackupProto(
    @ProtoNumber(1) val library: List<BookProto> = emptyList(),
    @ProtoNumber(2) val categories: List<CategoryProto> = emptyList()
) {
    fun toBackup() : Backup {
        return Backup(
            library,
            categories
        )
    }
}

