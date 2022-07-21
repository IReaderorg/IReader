package org.ireader.domain.use_cases.translate

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.delay
import org.ireader.common_data.TranslateEngine
import org.ireader.core_api.http.HttpClients
import java.net.URLEncoder

class TranslateDictUseCase(
    private val client: HttpClients
) : TranslateEngine {

    override val id: Long
        get() = -2

    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onSuccess: (List<String>) -> Unit
    ) {
        val result = texts.joinToString("\n").chunked(1000).map { text ->
            val url = "https://t2.translatedict.com/1.php?p1=$source&p2=$target&p3=${URLEncoder.encode(text,"utf-8")}"
            delay(1000)
            client.default.get(urlString = url) {}.bodyAsText()
        }.joinToString().split("\n")
        onSuccess(result)
    }
}