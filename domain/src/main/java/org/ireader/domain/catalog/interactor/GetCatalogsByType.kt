/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.domain.catalog.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.ireader.domain.catalog.model.CatalogSort
import org.ireader.domain.models.entities.CatalogInstalled
import org.ireader.domain.models.entities.CatalogLocal
import org.ireader.domain.models.entities.CatalogRemote

class GetCatalogsByType(
    private val localCatalogs: GetLocalCatalogs,
    private val remoteCatalogs: GetRemoteCatalogs,
) {

    suspend fun subscribe(
        sort: CatalogSort = CatalogSort.Favorites,
        excludeRemoteInstalled: Boolean = false,
        withNsfw: Boolean = true,
    ): Flow<Catalogs> {
        val localFlow = localCatalogs.subscribe(sort)
        val remoteFlow = remoteCatalogs.subscribe(withNsfw = withNsfw)
        return localFlow.combine(remoteFlow) { local, remote ->
            val (pinned, unpinned) = local
                .distinctBy { it.sourceId }
                .sortedByDescending { it.hasUpdate }
                .partition { it.isPinned }

            if (excludeRemoteInstalled) {
                val installedPkgs = local
                    .distinctBy { it.sourceId }
                    .asSequence()
                    .filterIsInstance<CatalogInstalled>()
                    .map { it.pkgName }
                    .toSet()

                Catalogs(pinned, unpinned, remote.filter { it.pkgName !in installedPkgs })
            } else {
                Catalogs(pinned, unpinned, remote)
            }
        }
    }

    data class Catalogs(
        val pinned: List<CatalogLocal>,
        val unpinned: List<CatalogLocal>,
        val remote: List<CatalogRemote>,
    )

}
