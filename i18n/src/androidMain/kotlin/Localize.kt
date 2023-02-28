/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.i18n


import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.*

// don't use this functions yet because moko-resource is not configured yet.


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

actual class LocalizeHelper(
    private val context: Context
) {

    actual fun localize(resId: Int): String {
        return context.getString(resId)
    }
    actual fun localize(resource: StringResource): String {
        return StringDesc.Resource(resource).toString(context)
    }

    actual fun localize(
        resource: StringResource,
        vararg args: Any
    ): String {
        return StringDesc.ResourceFormatted(resource, *args).toString(context)
    }

    actual fun localizePlural(
        resource: PluralsResource,
        quantity: Int
    ): String {
        return StringDesc.Plural(resource, quantity).toString(context)
    }

    actual fun localizePlural(
        resource: PluralsResource,
        quantity: Int,
        vararg args: Any
    ): String {
        return StringDesc.PluralFormatted(resource, quantity, *args).toString(context)
    }


}