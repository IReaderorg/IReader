/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.domain.catalog.interactor

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.catalog.model.InstallStep
import org.ireader.domain.catalog.service.CatalogInstaller
import org.ireader.domain.models.entities.CatalogRemote
import javax.inject.Inject

class InstallCatalog @Inject constructor(
    private val catalogInstaller: CatalogInstaller,
) {

    fun await(catalog: CatalogRemote): Flow<InstallStep> {
        return catalogInstaller.install(catalog)
    }

}
