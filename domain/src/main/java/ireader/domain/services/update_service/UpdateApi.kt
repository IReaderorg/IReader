package ireader.domain.services.update_service


import io.ktor.client.call.*
import io.ktor.client.request.*
import ireader.common.models.update_service_models.Release
import ireader.core.http.HttpClients
import ireader.i18n.github_api_url
import ireader.i18n.repo_url
import org.koin.core.annotation.Factory

@Factory
class UpdateApi(
    private val httpClients: HttpClients
) {
    suspend fun checkRelease(): Release {
        return httpClients.default.get(github_api_url + repo_url).body<Release>()
    }
}
