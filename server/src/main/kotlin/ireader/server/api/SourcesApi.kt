package ireader.server.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ireader.server.model.ErrorResponse
import kotlinx.serialization.Serializable

/**
 * API handler for source-related endpoints.
 *
 * Note: Full catalog integration requires platform-specific setup.
 * This is a placeholder implementation.
 */
class SourcesApi {
    fun registerRoutes(routing: Routing) {
        routing.route("/api/v1/sources") {
            // List all installed sources
            get {
                call.respond(emptyList<SourceDto>())
            }

            // Get source details
            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Invalid source ID"))
                    return@get
                }

                call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Source not found"))
            }
        }
    }
}

/**
 * Source DTO for API responses.
 */
@Serializable
data class SourceDto(
    val id: Long,
    val name: String,
    val lang: String,
    val supportsLatest: Boolean,
    val iconUrl: String? = null,
    val pkgName: String? = null,
    val versionName: String? = null
)


