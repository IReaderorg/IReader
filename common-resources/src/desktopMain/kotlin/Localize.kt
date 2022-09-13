/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.common.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

@Composable
@ReadOnlyComposable
actual fun localize(resource: StringResource): String {
  return StringDesc.Resource(resource).localized()
}

@Composable
@ReadOnlyComposable
actual fun localize(resource: StringResource, vararg args: Any): String {
  return StringDesc.ResourceFormatted(resource, *args).localized()
}

@Composable
@ReadOnlyComposable
actual fun localizePlural(resource: PluralsResource, quantity: Int): String {
  return StringDesc.Plural(resource, quantity).localized()
}

@Composable
@ReadOnlyComposable
actual fun localizePlural(resource: PluralsResource, quantity: Int, vararg args: Any): String {
  return StringDesc.PluralFormatted(resource, quantity, *args).localized()
}
