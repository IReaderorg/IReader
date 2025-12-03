/**
 * Common dependency configurations shared across modules.
 * This object centralizes dependency declarations to avoid duplication.
 */
object CommonDependencies {
    /**
     * Core Kotlin dependencies required by all modules
     */
    val kotlinCore = listOf(
        "kotlinx.coroutines.core",
        "kotlinx.stdlib",
        "kotlinx.datetime",
        "kotlinx.serialization.json"
    )
    
    /**
     * Networking dependencies for HTTP client functionality
     */
    val networking = listOf(
        "libs.ktor.core",
        "libs.ktor.contentNegotiation",
        "libs.ktor.contentNegotiation.kotlinx"
    )
    
    /**
     * Dependency injection with Koin
     */
    val dependencyInjection = listOf(
        "libs.koin.core"
    )
    
    /**
     * Common utilities
     */
    val utilities = listOf(
        "libs.okio",
        "libs.ksoup"
    )
}
