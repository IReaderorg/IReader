/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core.catalog.interactor

import org.ireader.common_models.entities.CatalogLocal
import org.ireader.core.catalog.service.CatalogStore
import javax.inject.Inject

class GetLocalCatalog @Inject constructor(
    private val store: CatalogStore,
) {

    fun get(sourceId: Long): CatalogLocal? {
        return store.get(sourceId)
    }

}
