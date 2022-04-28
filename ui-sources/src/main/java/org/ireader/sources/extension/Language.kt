/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.sources.extension


/* inline */
data class Language(val code: String) {

    fun toEmoji(): String? {
        val country = when (code) {
            "de" -> "DE"
            "fr" -> "FR"
            "en" -> "GB"
            "es" -> "ES"
            "it" -> "IT"
            "ja" -> "JP"
            "pt" -> "BR"
            "ru" -> "RU"
            "vi" -> "VN"
            "zh" -> "CN"
            else -> null
        }
        return country?.let { toFlag(it) }
    }

    private fun toFlag(countryCode: String): String {
        return try {
            val firstLetter = countryCode[0].code - 0x41 + 0x1F1E6
            val secondLetter = countryCode[1].code - 0x41 + 0x1F1E6
            String.fromCodePoints(firstLetter, secondLetter)
        } catch (e: Throwable) {
            ""
        }
    }

}

fun String.Companion.fromCodePoints(vararg codePoints: Int): String {
    var buffer = charArrayOf()
    for (codePoint in codePoints) {
        buffer += Character.toChars(codePoint)
    }
    return String(buffer)
}
