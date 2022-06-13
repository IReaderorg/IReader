package org.ireader.domain.use_cases.fonts

import io.ktor.client.call.body
import io.ktor.client.request.get
import org.ireader.core_api.http.HttpClients
import org.jsoup.Jsoup
import javax.inject.Inject

class FontUseCase @Inject constructor(
    private val clients: HttpClients,
) {

    suspend fun getRemoteFonts() : List<String> {

        val response : String = clients.default.get("https://fonts.gstatic.com/s/a/directory.xml", block = {}).body()
       return Jsoup.parse(response).select("family").eachAttr("name")
    }
}