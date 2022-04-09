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
import org.ireader.domain.catalog.service.CatalogPreferences
import org.ireader.domain.catalog.service.CatalogRemoteApi
import org.ireader.domain.catalog.service.CatalogRemoteRepository
import tachiyomi.core.log.Log
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncRemoteCatalogs @Inject constructor(
    private val catalogRemoteRepository: CatalogRemoteRepository,
    private val catalogRemoteApi: CatalogRemoteApi,
    private val catalogPreferences: CatalogPreferences,
) {

    suspend fun await(forceRefresh: Boolean): Boolean {
        val lastCheckPref = catalogPreferences.lastRemoteCheck()
        val lastCheck = lastCheckPref.get()
        val now = Calendar.getInstance().timeInMillis


        if (forceRefresh || (now - lastCheck) > TimeUnit.MINUTES.toMillis(5)) {
            try {
                withContext(Dispatchers.IO) {
                    val newCatalogs = catalogRemoteApi.fetchCatalogs()
                    catalogRemoteRepository.deleteAllRemoteCatalogs()
                    catalogRemoteRepository.insertRemoteCatalogs(newCatalogs)
                    lastCheckPref.set(Calendar.getInstance().timeInMillis)
                }
                return true
            } catch (e: Exception) {
                Log.warn(e, "Failed to fetch remote catalogs")
            }
        }

        return false
    }

}
