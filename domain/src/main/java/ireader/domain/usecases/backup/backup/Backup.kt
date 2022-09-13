package ireader.domain.usecases.backup.backup

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@ExperimentalSerializationApi
@Serializable
internal data class Backup(
    @ProtoNumber(1) val library: List<BookProto> = emptyList(),
    @ProtoNumber(2) val categories: List<CategoryProto> = emptyList()
)
