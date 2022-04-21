package org.ireader.domain.services.update_service

import io.ktor.client.call.*
import io.ktor.client.request.*
import org.ireader.core.utils.Constants.github_api_url
import org.ireader.core.utils.Constants.repo_url
import org.ireader.core_api.http.HttpClients
import org.ireader.domain.models.update_service_models.Release
import javax.inject.Inject

class UpdateApi @Inject constructor(
    private val httpClients: HttpClients
) {
    suspend fun checkRelease(): Release {
        return  httpClients.default.get(github_api_url+ repo_url).body<Release>()
    }
}
