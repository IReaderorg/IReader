package ireader.domain.services.update_service

import io.ktor.client.call.body
import io.ktor.client.request.get
import ireader.common.models.update_service_models.Release
import ireader.common.resources.github_api_url
import ireader.common.resources.repo_url
import ireader.core.api.http.HttpClients


class UpdateApi(
    private val httpClients: HttpClients
) {
    suspend fun checkRelease(): Release {
        return httpClients.default.get(github_api_url + repo_url).body<Release>()
    }
}
