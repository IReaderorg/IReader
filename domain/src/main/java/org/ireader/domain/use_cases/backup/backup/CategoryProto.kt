@file:OptIn(ExperimentalSerializationApi::class)

package org.ireader.domain.use_cases.backup.backup

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.ireader.common_models.entities.Category

@Serializable
internal data class CategoryProto(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val order: Int,
    @ProtoNumber(3) val updateInterval: Int = 0,
    @ProtoNumber(4) val flags: Long = 0
) {

    fun toDomain(): Category {
        return Category(
            name = name,
            order = order,
            updateInterval = updateInterval,
            flags = flags
        )
    }

    companion object {
        fun fromDomain(category: Category): CategoryProto {
            return CategoryProto(
                name = category.name,
                order = category.order,
                updateInterval = category.updateInterval,
                flags = category.flags
            )
        }
    }
}
