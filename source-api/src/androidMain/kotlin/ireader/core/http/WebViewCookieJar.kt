/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.http

import android.webkit.CookieManager
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.util.date.*
import ireader.core.util.createICoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl



class WebViewCookieJar(private val cookiesStorage: CookiesStorage) : CookieJar {

  private val manager: CookieManager = CookieManager.getInstance()

  override fun loadForRequest(url: HttpUrl): List<Cookie> {
    return get(url)
  }

  val scope = createICoroutineScope()
  override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {

    cookies.forEach { manager.setCookie(url.toString(), it.toString()) }
    scope.launch {
      cookies.forEach { cookiesStorage.addCookie(Url(url.toString()), it.toCookies()) }
    }
  }

  fun get(url: HttpUrl): List<Cookie> {
    val cookies = manager.getCookie(url.toString())

    return if (cookies != null && cookies.isNotEmpty()) {
      cookies.split(";").mapNotNull { Cookie.parse(url, it) }
    } else {
      emptyList()
    }
  }

  fun remove(url: HttpUrl, cookieNames: List<String>? = null, maxAge: Int = -1) {
    val urlString = url.toString()
    val cookies = manager.getCookie(urlString) ?: return

    fun Sequence<String>.filterNames(): Sequence<String> {
      return if (cookieNames != null) {
        this.filter { it in cookieNames }
      } else {
        this
      }
    }

    cookies.splitToSequence(";")
      .map { it.substringBefore("=") }
      .filterNames()
      .onEach { manager.setCookie(urlString, "$it=;Max-Age=$maxAge") }
  }

  fun removeAll() {
    manager.removeAllCookies {}
  }
}
fun Cookie.toCookies(): io.ktor.http.Cookie {
    return io.ktor.http.Cookie(
        this.name,
        this.value,
        httpOnly = this.httpOnly,
        domain = this.domain,
        expires = GMTDate(this.expiresAt),
        path = this.path,
        secure = this.secure

    )
}