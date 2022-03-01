/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.data.catalog

import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ireader.domain.catalog.service.CatalogRemoteApi
import org.ireader.domain.models.entities.CatalogRemote
import tachiyomi.core.http.HttpClients


class CatalogGithubApi(
    private val httpClients: HttpClients,
) : CatalogRemoteApi {

    private val repoUrl =
        "https://raw.githubusercontent.com/kazemcodes/IReader-Sources/main"

    override suspend fun fetchCatalogs(): List<CatalogRemote> {
        val response = httpClients.default.get<String>("$repoUrl/index.json")
        val catalogs = Json.Default.decodeFromString<List<CatalogRemoteApiModel>>(response)
        return catalogs.map { catalog ->
            CatalogRemote(
                name = catalog.name,
                description = catalog.description,
                sourceId = catalog.id,
                pkgName = catalog.pkg,
                versionName = catalog.version,
                versionCode = catalog.code,
                lang = catalog.lang,
                pkgUrl = catalog.apk,
                iconUrl = catalog.apk.replace(".apk", ".png"),
                nsfw = catalog.nsfw
            )
        }
    }

    @Serializable
    private data class CatalogRemoteApiModel(
        val name: String,
        val pkg: String,
        val version: String,
        val code: Int,
        val lang: String,
        val apk: String,
        val id: Long,
        val description: String,
        val nsfw: Boolean,
    )

}
