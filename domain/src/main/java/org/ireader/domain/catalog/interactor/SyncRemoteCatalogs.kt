/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.domain.catalog.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.ireader.core.utils.Constants
import org.ireader.domain.catalog.service.CatalogPreferences
import org.ireader.domain.catalog.service.CatalogRemoteApi
import org.ireader.domain.catalog.service.CatalogRemoteRepository
import tachiyomi.core.log.Log
import kotlin.time.ExperimentalTime

class SyncRemoteCatalogs(
    private val catalogRemoteRepository: CatalogRemoteRepository,
    private val catalogRemoteApi: CatalogRemoteApi,
    private val catalogPreferences: CatalogPreferences,
) {

    suspend fun await(forceRefresh: Boolean): Boolean {
        val lastCheckPref = catalogPreferences.lastRemoteCheck()
        val lastCheck = lastCheckPref.get()
        val now = System.currentTimeMillis()

        if (forceRefresh || now - lastCheck > minTimeApiCheck) {
            try {
                withContext(Dispatchers.IO) {
                    val newCatalogs = catalogRemoteApi.fetchCatalogs()
                    catalogRemoteRepository.deleteAllRemoteCatalogs()
                    catalogRemoteRepository.setRemoteCatalogs(newCatalogs)
                    lastCheckPref.set(Clock.System.now().toEpochMilliseconds())
                }
                return true
            } catch (e: Exception) {
                Log.warn(e, "Failed to fetch remote catalogs")
            }
        }

        return false
    }

    internal companion object {
        @OptIn(ExperimentalTime::class)
        val minTimeApiCheck = Constants.FIVE_MIN
    }

}
