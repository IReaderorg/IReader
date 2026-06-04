package ireader.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.Paths

fun main(args: Array<String>) {
    val config = ServerConfig.load(args)
    embeddedServer(Netty, port = config.port, host = config.host) {
        module(config)
    }.start(wait = true)
}

fun Application.module(config: ServerConfig) {
    val logger = LoggerFactory.getLogger("Server")

    // Install plugins
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        anyHost() // Allow all hosts for local network access
        allowHeader("Content-Type")
        allowHeader("Authorization")
    }

    install(Compression) {
        gzip()
        deflate()
    }

    install(DefaultHeaders) {
        header("X-Engine", "IReader-Server")
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Request failed", cause)
            call.respond(
                io.ktor.http.HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }

    // Configure routing
    routing {
        // Health check
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        // API routes
        route("/api/v1") {
            get("/info") {
                call.respond(
                    mapOf(
                        "name" to "IReader Server",
                        "version" to "1.0.0",
                        "port" to config.port
                    )
                )
            }

            // Sources endpoints
            route("/sources") {
                get {
                    call.respond(
                        mapOf(
                            "message" to "Sources endpoint - coming soon",
                            "sources" to emptyList<String>()
                        )
                    )
                }
            }

            // Books endpoints
            route("/books") {
                get {
                    call.respond(
                        mapOf(
                            "message" to "Books endpoint - coming soon",
                            "books" to emptyList<String>()
                        )
                    )
                }
            }
        }

        // Static files (React UI) - serve from resources/static
        staticFiles("/", javaClass.classLoader.getResource("static")?.let {
            java.io.File(it.toURI())
        } ?: java.io.File("src/main/resources/static")) {
            default("index.html")
        }
    }

    logger.info("IReader Server started on ${config.host}:${config.port}")
    logger.info("Access the UI at http://localhost:${config.port}")
}

data class ServerConfig(
    val port: Int,
    val host: String,
    val dataDir: Path,
    val sourcesDir: Path
) {
    companion object {
        fun load(args: Array<String>): ServerConfig {
            val port = args.find { it.startsWith("--port=") }
                ?.substringAfter("=")
                ?.toIntOrNull()
                ?: System.getenv("IREADER_PORT")?.toIntOrNull()
                ?: 8080

            val host = args.find { it.startsWith("--host=") }
                ?.substringAfter("=")
                ?: System.getenv("IREADER_HOST")
                ?: "0.0.0.0" // Listen on all interfaces for mobile access

            val dataDir = args.find { it.startsWith("--data-dir=") }
                ?.substringAfter("=")
                ?.let { Paths.get(it) }
                ?: System.getenv("IREADER_DATA_DIR")?.let { Paths.get(it) }
                ?: Paths.get(System.getProperty("user.home"), ".ireader", "server")

            val sourcesDir = args.find { it.startsWith("--sources-dir=") }
                ?.substringAfter("=")
                ?.let { Paths.get(it) }
                ?: dataDir.resolve("sources")

            return ServerConfig(
                port = port,
                host = host,
                dataDir = dataDir,
                sourcesDir = sourcesDir
            )
        }
    }
}
