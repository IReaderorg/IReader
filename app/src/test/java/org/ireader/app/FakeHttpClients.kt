package org.ireader.app

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.BrowserUserAgent
import org.ireader.core_api.http.main.BrowseEngine
import org.ireader.core_api.http.main.HttpClients

class FakeHttpClients : HttpClients {
    override val browser: BrowseEngine
        get() = throw Exception()
    override val default: HttpClient
        get()  = HttpClient(CIO) {
            BrowserUserAgent()
        }
    override val cloudflareClient: HttpClient
        get() = HttpClient(CIO) {
            BrowserUserAgent()
        }
}