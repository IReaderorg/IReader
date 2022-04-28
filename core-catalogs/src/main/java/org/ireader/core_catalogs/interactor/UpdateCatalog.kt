/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_catalogs.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.ireader.common_models.entities.CatalogInstalled
import org.ireader.core_catalogs.model.InstallStep
import org.ireader.core_catalogs.service.CatalogRemoteRepository
import javax.inject.Inject

class UpdateCatalog @Inject constructor(
    private val catalogRemoteRepository: CatalogRemoteRepository,
    private val installCatalog: InstallCatalog,
) {

    suspend fun await(catalog: CatalogInstalled, onError: (Throwable) -> Unit): Flow<InstallStep> {
        val catalogs = catalogRemoteRepository.getRemoteCatalogs()

        val catalogToUpdate = catalogs.find { it.pkgName == catalog.pkgName }
        return if (catalogToUpdate == null) {
            emptyFlow()
        } else {
            installCatalog.await(catalogToUpdate,onError)
        }
    }

}
