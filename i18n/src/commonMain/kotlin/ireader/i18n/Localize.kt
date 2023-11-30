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
expect fun localize(resource: (XmlStrings) -> String): String

@Composable
expect fun localize(resource: (XmlStrings) -> String, vararg args: Any): String

@Composable
expect fun localizePlural(resource: (XmlStrings) -> String, quantity: Int): String

@Composable
expect fun localizePlural(resource: (XmlStrings) -> String, quantity: Int, vararg args: Any): String


expect class LocalizeHelper {
    var xml: XmlStrings?

    @Composable
    fun Init()
    fun localize(resId: Int): String
    fun localize(resource: (XmlStrings) -> String): String

    fun localize(resource: (XmlStrings) -> String, vararg args: Any): String

    fun localizePlural(resource: (XmlStrings) -> String, quantity: Int): String

    fun localizePlural(resource: (XmlStrings) -> String, quantity: Int, vararg args: Any): String


}