package ireader.server.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ireader.server.model.ErrorResponse
import kotlinx.serialization.Serializable

/**
 * API handler for chapter-related endpoints.
 *
 * Note: Full database integration requires platform-specific setup.
 * This is a placeholder implementation.
 */
class ChaptersApi {
    fun registerRoutes(routing: Routing) {
        routing.route("/api/v1/chapters") {
            // Get chapter pages
            get("/{id}/pages") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Invalid chapter ID"))
                    return@get
                }

                call.respond(emptyList<PageDto>())
            }

            // Mark chapter as read
            put("/{id}/read") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Invalid chapter ID"))
                    return@put
                }

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

@Serializable
data class PageDto(
    val index: Int,
    val url: String
)


