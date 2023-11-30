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


@Composable
actual fun localize(resource: (XmlStrings) -> String): String {
    val lyricist = LocalXmlStrings.current
    return resource(lyricist)
}


@Composable
actual fun localize(resource: (XmlStrings) -> String, vararg args: Any): String {
    val lyricist = LocalXmlStrings.current
    return resource(lyricist)
}

@Composable
actual fun localizePlural(resource: (XmlStrings) -> String, quantity: Int): String {
    val lyricist = LocalXmlStrings.current
    return resource(lyricist).replace("%1\$d", quantity.toString())
}

@Composable
actual fun localizePlural(
    resource: (XmlStrings) -> String,
    quantity: Int,
    vararg args: Any
): String {
    val lyricist = LocalXmlStrings.current
    return resource(lyricist)
}

actual class LocalizeHelper(
    private val context: Context
) {

    actual fun localize(resId: Int): String {
        return context.getString(resId)
    }

    actual fun localize(resource: (XmlStrings) -> String): String {
        if (xml == null) return ""
        return resource(xml!!)
    }

    actual fun localize(
        resource: (XmlStrings) -> String,
        vararg args: Any
    ): String {
        if (xml == null) return ""
        return resource(xml!!)
    }

    actual fun localizePlural(
        resource: (XmlStrings) -> String,
        quantity: Int
    ): String {
        if (xml == null) return ""
        return resource(xml!!)
    }

    actual fun localizePlural(
        resource: (XmlStrings) -> String,
        quantity: Int,
        vararg args: Any
    ): String {
        if (xml == null) return ""
        return resource(xml!!).replace("%1\$d", quantity.toString())
    }

    actual var xml: XmlStrings? = null

    @Composable
    actual fun Init() {
        xml = LocalXmlStrings.current
    }


}