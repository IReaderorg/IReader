package ireader.domain.usecases.fonts

import io.ktor.client.call.*
import io.ktor.client.request.*
import ireader.core.http.HttpClients
import org.jsoup.Jsoup



class FontUseCase(
    private val clients: HttpClients,
) {

    suspend fun getRemoteFonts(): List<String> {

        val response: String = clients.default.get("https://fonts.gstatic.com/s/a/directory.xml", block = {}).body()
        return Jsoup.parse(response).select("family").eachAttr("name")
    }
}
