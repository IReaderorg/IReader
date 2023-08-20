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
import dev.icerock.moko.resources.desc.*

@Composable
actual fun localize(resource: StringResource): String {
  return StringDesc.Resource(resource).localized()
}

@Composable
actual fun localize(resource: StringResource, vararg args: Any): String {
  return StringDesc.ResourceFormatted(resource, *args).localized()
}

@Composable
actual fun localizePlural(resource: PluralsResource, quantity: Int): String {
  return StringDesc.Plural(resource, quantity).localized()
}

@Composable
actual fun localizePlural(resource: PluralsResource, quantity: Int, vararg args: Any): String {
  return StringDesc.PluralFormatted(resource, quantity, *args).localized()
}

actual class LocalizeHelper {
    actual fun localize(resource: StringResource): String {
        return StringDesc.Resource(resource).localized()
    }

    actual fun localize(
        resource: StringResource,
        vararg args: Any
    ): String {
        return StringDesc.ResourceFormatted(resource, *args).localized()
    }

    actual fun localizePlural(
        resource: PluralsResource,
        quantity: Int
    ): String {
        return StringDesc.Plural(resource, quantity).localized()
    }

    actual fun localizePlural(
        resource: PluralsResource,
        quantity: Int,
        vararg args: Any
    ): String {
        return StringDesc.PluralFormatted(resource, quantity, *args).localized()
    }

    actual fun localize(resId: Int): String {
       return "NOT SUPPORTED"
    }


}