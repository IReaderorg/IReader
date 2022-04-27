/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.data.catalog

import android.app.Application
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core_api.http.HttpClients
import org.ireader.core_api.io.saveTo
import org.ireader.core_api.log.Log
import org.ireader.core_api.os.PackageInstaller
import org.ireader.domain.catalog.model.InstallStep
import org.ireader.domain.catalog.service.CatalogInstaller
import org.ireader.domain.models.entities.CatalogRemote
import java.io.File
import javax.inject.Inject

/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */

class AndroidCatalogInstaller @Inject constructor(
    private val context: Application,
    private val httpClient: HttpClients,
    private val installationChanges: AndroidCatalogInstallationChanges,
    private val packageInstaller : PackageInstaller
) : CatalogInstaller {

    /**
     * The client used for http requests.
     */
    private val client get() = httpClient.default

    /**
     * Adds the given extension to the downloads queue and returns an observable containing its
     * step in the installation process.
     *
     * @param catalog The catalog to install.
     */
    override fun install(catalog: CatalogRemote, onError: (Throwable) -> Unit): Flow<InstallStep> = flow {
        emit(InstallStep.Downloading)
        val tmpApkFile = File(context.cacheDir, "${catalog.pkgName}.apk")
        val tmpIconFile = File(context.cacheDir, "${catalog.pkgName}.png")
        try {
            val apkResponse : ByteReadChannel = client.get(catalog.pkgUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            apkResponse.saveTo(tmpApkFile)

            val iconResponse : ByteReadChannel = client.get(catalog.iconUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            iconResponse.saveTo(tmpIconFile)

            emit(InstallStep.Installing)

//            val extDir = File(context.filesDir, "catalogs/${catalog.pkgName}").apply { mkdirs() }
//            val apkFile = File(extDir, tmpApkFile.name)
//            val iconFile = File(extDir, tmpIconFile.name)
            val success = packageInstaller.install(tmpApkFile,catalog.pkgName)

            tmpApkFile.deleteRecursively()
            tmpIconFile.deleteRecursively()
           // val apkSuccess = tmpApkFile.renameTo(apkFile)
          //  val iconSuccess = tmpIconFile.renameTo(iconFile)
         //   val success = apkSuccess && iconSuccess
            if (success) {
                installationChanges.notifyAppInstall(catalog.pkgName)
            }
            emit(if (success) InstallStep.Completed else InstallStep.Error)
        } catch (e: Throwable) {
            Log.warn(e, "Error installing package")
            onError(e)
            emit(InstallStep.Error)
        } finally {
            tmpApkFile.delete()
            tmpIconFile.delete()
        }
    }

    /**
     * Starts an intent to uninstall the extension by the given package name.
     *
     * @param pkgName The package name of the extension to uninstall
     */
    override suspend fun uninstall(pkgName: String, onError: (Throwable) -> Unit): Boolean {
        return try {
            val deleted = packageInstaller.uninstall(pkgName)
            installationChanges.notifyAppUninstall(pkgName)
            deleted
        }catch (e:Throwable) {
            onError(e)
            false
        }
//        val file = File(context.filesDir, "catalogs/${pkgName}")
//        val deleted = file.deleteRecursively()

    }

}
