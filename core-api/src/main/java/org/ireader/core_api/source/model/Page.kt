/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.source.model

sealed class Page

data class PageUrl(val url: String) : Page()
sealed class PageComplete : Page()

data class ImageUrl(val url: String) : PageComplete()
data class ImageBase64(val data: String) : PageComplete()
data class Text(val text: String) : PageComplete()
