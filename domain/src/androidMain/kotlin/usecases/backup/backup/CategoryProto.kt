


package ireader.domain.usecases.backup.backup

import ireader.domain.models.entities.Category
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
internal data class CategoryProto(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val order: Long,
    @ProtoNumber(3) val updateInterval: Int = 0,
    @ProtoNumber(4) val flags: Long = 0
) {

    fun toDomain(): Category {
        return Category(
            name = name,
            order = order,
            flags = flags,
        )
    }

    companion object {
        fun fromDomain(category: Category): CategoryProto {
            return CategoryProto(
                name = category.name,
                order = category.order,
                flags = category.flags
            )
        }
    }

}



