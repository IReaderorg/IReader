package ireader.domain.usecases.fonts

import io.ktor.client.call.body
import io.ktor.client.request.get
import ireader.core.api.http.HttpClients
import org.jsoup.Jsoup
import org.koin.core.annotation.Factory

@Factory
class FontUseCase(
    private val clients: HttpClients,
) {

    suspend fun getRemoteFonts(): List<String> {

        val response: String = clients.default.get("https://fonts.gstatic.com/s/a/directory.xml", block = {}).body()
        return Jsoup.parse(response).select("family").eachAttr("name")
    }
}
