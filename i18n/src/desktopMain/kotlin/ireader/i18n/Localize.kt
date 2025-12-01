/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.i18n

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import kotlinx.coroutines.runBlocking

@Composable
actual fun localize(resource: StringResource): String {
  return stringResource(resource)
}

@Composable
actual fun localize(resource: StringResource, vararg args: Any): String {
  return stringResource(resource, *args)
}

@Composable
actual fun localizePlural(resource: PluralStringResource, quantity: Int): String {
  return pluralStringResource(resource, quantity)
}

@Composable
actual fun localizePlural(resource: PluralStringResource, quantity: Int, vararg args: Any): String {
  return pluralStringResource(resource, quantity, *args)
}

actual class LocalizeHelper {
    actual fun localize(resource: StringResource): String = runBlocking {
        org.jetbrains.compose.resources.getString(resource)
    }

    actual fun localize(
        resource: StringResource,
        vararg args: Any
    ): String = runBlocking {
        org.jetbrains.compose.resources.getString(resource, *args)
    }

    actual fun localizePlural(
        resource: PluralStringResource,
        quantity: Int
    ): String = runBlocking {
        org.jetbrains.compose.resources.getPluralString(resource, quantity)
    }

    actual fun localizePlural(
        resource: PluralStringResource,
        quantity: Int,
        vararg args: Any
    ): String = runBlocking {
        org.jetbrains.compose.resources.getPluralString(resource, quantity, *args)
    }

    actual fun localize(resId: Int): String {
       return "NOT SUPPORTED"
    }

    actual fun getCurrentLanguageCode(): String {
        return java.util.Locale.getDefault().language
    }

}