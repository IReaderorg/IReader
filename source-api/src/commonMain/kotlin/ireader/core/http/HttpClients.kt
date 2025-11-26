/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.http

import io.ktor.client.*

/**
 * Unified HTTP client interface providing different client configurations
 * for various use cases (default, Cloudflare bypass, browser engine)
 */
interface HttpClientsInterface {
  val browser: BrowserEngine
  val default: HttpClient
  val cloudflareClient: HttpClient
  val config: NetworkConfig
  val sslConfig: SSLConfiguration
  val cookieSynchronizer: CookieSynchronizer
}

expect class HttpClients : HttpClientsInterface {
  override val browser: BrowserEngine
  override val default: HttpClient
  override val cloudflareClient: HttpClient
  override val config: NetworkConfig
  override val sslConfig: SSLConfiguration
  override val cookieSynchronizer: CookieSynchronizer
}
