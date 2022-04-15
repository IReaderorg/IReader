/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.source

import io.ktor.client.*
import io.ktor.client.request.*
import org.ireader.core_api.source.model.ImageUrl
import org.ireader.core_api.source.model.Listing
import org.ireader.core_api.source.model.PageComplete
import org.ireader.core_api.source.model.PageUrl


/**
 * A simple implementation for sources from a website.
 */
@Suppress("unused", "unused_parameter")
abstract class HttpSource(private val dependencies: Dependencies) : CatalogSource {

  /**
   * Base url of the website without the trailing slash, like: http://mysite.com
   */
  abstract val baseUrl: String

  /**
   * Version id used to generate the source id. If the site completely changes and urls are
   * incompatible, you may increase this value and it'll be considered as a new source.
   */
  open val versionId = 1

  /**
   * Default network client for doing requests.
   */
  open val client: HttpClient
    get() = dependencies.httpClients.default

  /**
   * Visible name of the source.
   */
  override fun toString() = "$name (${lang.uppercase()})"

  open suspend fun getPage(page: PageUrl): PageComplete {
    throw Exception("Incomplete source implementation. Please override getPage when using PageUrl")
  }

  open fun getImageRequest(page: ImageUrl): Pair<HttpClient, HttpRequestBuilder> {
    return client to HttpRequestBuilder().apply {
      url(page.url)
    }
  }

  open fun getCoverRequest(url: String): Pair<HttpClient, HttpRequestBuilder> {
    return client to HttpRequestBuilder().apply {
      url(url)
    }
  }

  override fun getListings(): List<Listing> {
    return emptyList()
  }

}
