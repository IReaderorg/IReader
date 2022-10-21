package ireader.data.catalogrepository

import ireader.domain.models.entities.ExtensionSource

val catalogRemoteMapper =
    { _id: Long, name: String, key: String, owner: String, source: String, last_update: Long, is_enable: Boolean ->

        ExtensionSource(
            _id,
            name,
            key,
            owner,
            source,
            last_update,
            is_enable
        )
    }
