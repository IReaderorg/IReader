/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.i18n


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

@Composable
actual fun localize(resource: StringResource): String {
  return StringDesc.Resource(resource).toString(LocalContext.current)
}

@Composable
actual fun localize(resource: StringResource, vararg args: Any): String {
  return StringDesc.ResourceFormatted(resource, *args).toString(LocalContext.current)
}

@Composable
actual fun localizePlural(resource: PluralsResource, quantity: Int): String {
  return StringDesc.Plural(resource, quantity).toString(LocalContext.current)
}

@Composable
actual fun localizePlural(resource: PluralsResource, quantity: Int, vararg args: Any): String {
  return StringDesc.PluralFormatted(resource, quantity, *args).toString(LocalContext.current)
}
