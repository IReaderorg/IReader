/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_catalogs.interactor

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.core_catalogs.model.InstallStep
import org.ireader.core_catalogs.service.CatalogInstaller
import javax.inject.Inject

class InstallCatalog @Inject constructor(
    private val catalogInstaller: CatalogInstaller,
) {

    fun await(catalog: CatalogRemote, onError: (Throwable) -> Unit): Flow<InstallStep> {
        return catalogInstaller.install(catalog,onError )
    }

}
