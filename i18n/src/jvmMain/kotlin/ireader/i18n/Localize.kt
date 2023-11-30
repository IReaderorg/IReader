/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.i18n

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
    return resource(lyricist)
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

actual class LocalizeHelper() {


    actual fun localize(resource: (XmlStrings) -> String): String {
        return resource(xml!!)
    }

    actual fun localize(
        resource: (XmlStrings) -> String,
        vararg args: Any
    ): String {
        return resource(xml!!)
    }

    actual fun localizePlural(
        resource: (XmlStrings) -> String,
        quantity: Int
    ): String {
        return resource(xml!!)
    }

    actual fun localizePlural(
        resource: (XmlStrings) -> String,
        quantity: Int,
        vararg args: Any
    ): String {
        return resource(xml!!).replace("%1\$d", quantity.toString())
    }

    actual fun localize(resId: Int): String {
        return "NOT SUPPORTED"
    }

    actual var xml: XmlStrings? = EnXmlStrings

    @Composable
    actual fun Init() {
        xml = LocalXmlStrings.current
    }


}