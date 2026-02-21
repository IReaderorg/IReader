package ireader.presentation.core

/**
 * Navigation routes for the application.
 * 
 * These are the route strings used with Compose Navigation.
 */
object NavigationRoutes {
    const val changelog = "changelog"
    const val cloudBackup = "cloudBackup"
    const val googleDriveBackup = "googleDriveBackup"
    const val translationSettings = "translationSettings"
    const val backupRestore = "backupRestore"
    const val donation = "donation"
    const val ttsEngineManager = "ttsEngineManager"
    const val profile = "profile"
    const val auth = "auth"
    const val supabaseConfig = "supabaseConfig"
    const val badgeStore = "badgeStore"
    const val nftBadge = "nftBadge"
    const val badgeManagement = "badgeManagement"
    const val adminBadgeVerification = "adminBadgeVerification"
    const val leaderboard = "leaderboard"
    const val popularBooks = "popularBooks"
    const val allReviews = "allReviews"
    const val fontSettings = "fontSettings"
    const val generalSettings = "generalSettings"
    const val readerSettings = "readerSettings"
    const val securitySettings = "securitySettings"
    const val advanceSettings = "advanceSettings"
    const val jsPluginSettings = "jsPluginSettings"
    const val repositoryAdd = "repositoryAdd"
    const val about = "about"
    const val settings = "settings"
    const val category = "category"
    const val downloader = "downloader"
    const val appearance = "appearance"
    const val repository = "repository"
    const val webView = "webView"
    const val browseSettings = "browseSettings"
    const val trackingSettings = "trackingSettings"
    const val readingHub = "readingHub"
    const val quotesScreen = "quotesScreen"
    const val submitQuote = "submitQuote"
    const val communityHub = "communityHub"
    const val characterArtGallery = "characterArtGallery"
    const val characterArtUpload = "characterArtUpload"
    const val characterArtDetail = "characterArtDetail"
    const val adminCharacterArtVerification = "adminCharacterArtVerification"
    
    /**
     * Navigate to character art upload with pre-filled data from chapter.
     * Uses Base64 encoding for prompt to handle special characters safely.
     */
    fun characterArtUploadWithData(
        bookTitle: String,
        chapterTitle: String,
        prompt: String
    ): String {
        // Simple URL encoding for title fields
        fun encodeSimple(s: String) = s
            .replace("&", "%26")
            .replace("=", "%3D")
            .replace("?", "%3F")
            .replace("/", "%2F")
            .replace(" ", "%20")
        
        // Base64 encode the prompt to handle all special characters
        val encodedPrompt = kotlin.io.encoding.Base64.encode(prompt.encodeToByteArray())
        
        return "characterArtUpload?bookTitle=${encodeSimple(bookTitle)}&chapterTitle=${encodeSimple(chapterTitle)}&prompt=$encodedPrompt"
    }
    const val glossary = "glossary"
    const val communitySourceConfig = "communitySourceConfig"
    const val adminUserPanel = "adminUserPanel"
    const val cloudflareBypass = "cloudflareBypass"
    const val wifiSync = "wifiSync"
    const val featureStore = "featureStore"
    const val pluginRepository = "pluginRepository"
    const val developerPortal = "developerPortal"
    const val userSources = "userSources"
    const val userSourceCreator = "userSourceCreator"
    const val legadoSourceImport = "legadoSourceImport"
    const val PLUGIN_DETAILS = "pluginDetails/{pluginId}"
    const val PLUGIN_DETAILS_BASE = "pluginDetails"
    const val pluginManagement = "pluginManagement"
    const val quoteCreation = "quoteCreation"
    const val myQuotes = "myQuotes"
    
    fun quoteCreation(params: ireader.domain.models.quote.QuoteCreationParams): String {
        val encodedBookTitle = params.bookTitle.encodeForNav()
        val encodedChapterTitle = params.chapterTitle.encodeForNav()
        val encodedAuthor = params.author?.encodeForNav()
        
        return buildString {
            append("$quoteCreation?bookId=${params.bookId}")
            append("&bookTitle=$encodedBookTitle")
            append("&chapterTitle=$encodedChapterTitle")
            params.chapterNumber?.let { append("&chapterNumber=$it") }
            encodedAuthor?.let { append("&author=$it") }
            params.currentChapterId?.let { append("&currentChapterId=$it") }
            params.prevChapterId?.let { append("&prevChapterId=$it") }
            params.nextChapterId?.let { append("&nextChapterId=$it") }
        }
    }
    
    private fun String.encodeForNav(): String = this
        .replace("%", "%25")
        .replace("&", "%26")
        .replace("=", "%3D")
        .replace("?", "%3F")
        .replace("/", "%2F")
        .replace(" ", "%20")
        .replace("#", "%23")
    
    fun pluginDetails(pluginId: String) = "pluginDetails/$pluginId"
    
    // Routes with parameters
    fun bookDetail(bookId: Long) = "bookDetail/$bookId"
    fun webView(
        url: String,
        sourceId: Long? = null,
        bookId: Long? = null,
        chapterId: Long? = null,
        enableBookFetch: Boolean = false,
        enableChapterFetch: Boolean = false,
        enableChaptersFetch: Boolean = false
    ): String {
        val params = buildList {
            add("url=$url")
            sourceId?.let { add("sourceId=$it") }
            bookId?.let { add("bookId=$it") }
            chapterId?.let { add("chapterId=$it") }
            if (enableBookFetch) add("enableBookFetch=true")
            if (enableChapterFetch) add("enableChapterFetch=true")
            if (enableChaptersFetch) add("enableChaptersFetch=true")
        }.joinToString("&")
        return "webView?$params"
    }
    fun reader(bookId: Long, chapterId: Long) = "reader/$bookId/$chapterId"
    fun explore(sourceId: Long) = "explore/$sourceId"
    fun tts(bookId: Long, chapterId: Long, sourceId: Long, readingParagraph: Int) = 
        "tts/$bookId/$chapterId/$sourceId/$readingParagraph"
    fun ttsV2(bookId: Long, chapterId: Long, sourceId: Long, readingParagraph: Int = 0) = 
        "ttsV2/$bookId/$chapterId/$sourceId/$readingParagraph"
    fun sourceDetail(sourceId: Long) = "sourceDetail/$sourceId"
    
    // Route patterns for composable definitions
    const val MAIN = "main"
    const val main = "main"
    const val READER = "reader/{bookId}/{chapterId}"
    const val BOOK_DETAIL = "bookDetail/{bookId}"
    const val TTS = "tts/{bookId}/{chapterId}/{sourceId}/{readingParagraph}"
    const val TTS_V2 = "ttsV2/{bookId}/{chapterId}/{sourceId}/{readingParagraph}"
    const val DOWNLOADER = "downloader"
    const val EXPLORE = "explore/{sourceId}"
    const val SOURCE_DETAIL = "sourceDetail/{sourceId}"
    const val SOURCE_MIGRATION = "sourceMigration/{sourceId}"
    const val GLOBAL_SEARCH = "globalSearch"
    
    // Base route names for popUpTo operations (without parameters)
    const val READER_BASE = "reader"
    const val BOOK_DETAIL_BASE = "bookDetail"
    const val TTS_BASE = "tts"
    const val TTS_V2_BASE = "ttsV2"
    const val EXPLORE_BASE = "explore"
    const val SOURCE_DETAIL_BASE = "sourceDetail"
    const val SOURCE_MIGRATION_BASE = "sourceMigration"
}
