package org.ireader.core_api.http.main

import io.ktor.client.HttpClient

interface HttpClients {
    val browser: BrowseEngine
    val default: HttpClient
    val cloudflareClient: HttpClient
}