package org.ireader.domain.services.update_service

import io.ktor.client.call.body
import io.ktor.client.request.get
import org.ireader.common_models.update_service_models.Release
import org.ireader.common_resources.github_api_url
import org.ireader.common_resources.repo_url
import org.ireader.core_api.http.main.HttpClients
import javax.inject.Inject

class UpdateApi @Inject constructor(
    private val httpClients: HttpClients
) {
    suspend fun checkRelease(): Release {
        return httpClients.default.get(github_api_url + repo_url).body<Release>()
    }
}
