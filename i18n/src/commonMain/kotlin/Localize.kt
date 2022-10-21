/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package ireader.i18n

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource


@Composable
expect fun localize(resource: StringResource): String

@Composable
expect fun localize(resource: StringResource, vararg args: Any): String

@Composable
expect fun localizePlural(resource: PluralsResource, quantity: Int): String

@Composable
expect fun localizePlural(resource: PluralsResource, quantity: Int, vararg args: Any): String

