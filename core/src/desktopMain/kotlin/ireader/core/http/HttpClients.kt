/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.http

import io.ktor.client.*

actual class HttpClients() : HttpClientsInterface {
  actual override val default = HttpClient()
  actual override val browser: BrowserEngine
    get() = TODO("Not yet implemented")
  actual override val cloudflareClient: HttpClient
    get() = TODO("Not yet implemented")
}
