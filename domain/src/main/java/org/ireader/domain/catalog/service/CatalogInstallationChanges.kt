/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.domain.catalog.service

import androidx.annotation.Keep
import kotlinx.coroutines.flow.SharedFlow

interface CatalogInstallationChanges {
    val flow: SharedFlow<CatalogInstallationChange>
}

@Keep
sealed class CatalogInstallationChange {
    abstract val pkgName: String

    @Keep
    data class SystemInstall(override val pkgName: String) : CatalogInstallationChange()
    @Keep
    data class SystemUninstall(override val pkgName: String) : CatalogInstallationChange()

    @Keep
    data class LocalInstall(override val pkgName: String) : CatalogInstallationChange()
    @Keep
    data class LocalUninstall(override val pkgName: String) : CatalogInstallationChange()
}
