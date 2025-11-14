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
    const val fontSettings = "fontSettings"
    const val generalSettings = "generalSettings"
    const val readerSettings = "readerSettings"
    const val securitySettings = "securitySettings"
    const val advanceSettings = "advanceSettings"
    const val repositoryAdd = "repositoryAdd"
    const val about = "about"
    const val settings = "settings"
    const val category = "category"
    const val downloader = "downloader"
    const val appearance = "appearance"
    const val statistics = "statistics"
    const val repository = "repository"
    const val webView = "webView"
    
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
    fun sourceDetail(sourceId: Long) = "sourceDetail/$sourceId"
    
    // Constants for Android compatibility
    const val MAIN = "main"
    const val READER = "reader/{bookId}/{chapterId}"
    const val BOOK_DETAIL = "bookDetail/{bookId}"
    const val TTS = "tts/{bookId}/{chapterId}/{sourceId}/{readingParagraph}"
    const val DOWNLOADER = "downloader"
}
