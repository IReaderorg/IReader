/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core.catalog.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.core.catalog.service.CatalogRemoteRepository
import javax.inject.Inject

class GetRemoteCatalogs @Inject constructor(
    private val catalogRemoteRepository: CatalogRemoteRepository,
) {

    suspend fun await(): List<CatalogRemote> {
        return catalogRemoteRepository.getRemoteCatalogs()
    }

    fun subscribe(
        withNsfw: Boolean = true,
    ): Flow<List<CatalogRemote>> {
        return catalogRemoteRepository.getRemoteCatalogsFlow()
            .map { catalogs ->
                if (withNsfw) {
                    catalogs.distinctBy { it.sourceId }
                } else {
                    catalogs.filter { !it.nsfw }
                }
            }
    }

}
