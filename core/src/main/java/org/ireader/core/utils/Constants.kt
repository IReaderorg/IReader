package org.ireader.core.utils

import androidx.compose.ui.unit.dp

object Constants {

    const val SHARED_PREF_NAME = "shared_pref_name"


    val DEFAULT_ELEVATION = 0.dp
    const val CLOUDFLARE_LOG = "Performance & security by Cloudflare"

    const val ERROR_LOG = "ERROR_LOG "
    const val DEBUG_LOG = "DEBUG_LOG "


    const val KODEIN_MODULE = "kodein_module"

    const val ImageKeyTable = "images_key_table"
    const val BOOK_TABLE = "library"
    const val CHAPTER_TABLE = "chapter"
    const val DOWNLOAD_TABLE = "download"
    const val CATALOG_REMOTE = "catalog_remote"
    const val UPDATE_TABLE = "update_table"
    const val HISTORY_TABLE = "history"
    const val CATEGORY_TABLE = "category_table"

    const val EXPLORE_BOOK_TABLE = "explore"

    const val PAGE_KET_TABLE = "page_key_table"

    const val DEFAULT_PAGE_SIZE = 6
    const val DEFAULT_BIG_PAGE_SIZE = 150
    const val MAX_PAGE_SIZE = 150
    const val MAX_BIG_PAGE_SIZE = 450


    const val ARG_HIDE_BOTTOM_BAR = "ARG_HIDE_BOTTOM_BAR"


    const val NO_BOOKS_ERROR = "There is no book"
    const val NO_BOOK_ERROR = "There is no book with this name"
    const val NO_CHAPTER_ERROR = "There is No chapter with this name"
    const val NO_CHAPTERS_ERROR = "There are no chapters with this name"

    const val CLOUDFLARE_PROTECTION_ERROR =
        "Cloudflare site protection is enable, Please Open the WebView."

    const val UNKNOWN_ERROR = "Unknown Error Happened"

    const val repo_url = "/repos/kazemcodes/IReader/releases/latest"
    const val github_api_url = "https://api.github.com"

    const val NULL_VALUE = -1L
    const val LAST_CHAPTER = -2L
    const val NO_VALUE = 0L

    const val BACKUP_CODE = 666
    const val RESTORE_CODE = 665
    const val PARSE_CONTENT = "PARSE_CONTENT"
    const val PARSE_DETAIL = "PARSE_DETAIL"
    const val PARSE_CHAPTERS = "PARSE_CHAPTERS"


}


object DEFAULT {
    const val MAX_BRIGHTNESS = .5F
    const val MIN_BRIGHTNESS = 0F
}