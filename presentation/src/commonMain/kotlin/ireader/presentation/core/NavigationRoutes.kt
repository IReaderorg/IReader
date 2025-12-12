package ireader.presentation.core

/**
 * Navigation routes for the application.
 * 
 * These are the route strings used with Compose Navigation.
 */
object NavigationRoutes {
    const val changelog = "changelog"
    const val cloudBackup = "cloudBackup"
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
    const val statistics = "statistics"
    const val repository = "repository"
    const val webView = "webView"
    const val browseSettings = "browseSettings"
    const val readingBuddy = "readingBuddy"
    const val adminQuoteVerification = "adminQuoteVerification"
    const val communityHub = "communityHub"
    const val characterArtGallery = "characterArtGallery"
    const val characterArtUpload = "characterArtUpload"
    const val characterArtDetail = "characterArtDetail"
    const val adminCharacterArtVerification = "adminCharacterArtVerification"
    const val glossary = "glossary"
    const val adminUserPanel = "adminUserPanel"
    
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
