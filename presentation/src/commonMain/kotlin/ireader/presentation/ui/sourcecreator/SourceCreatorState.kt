package ireader.presentation.ui.sourcecreator

import androidx.compose.runtime.Stable
import ireader.domain.usersource.model.*

/**
 * UI State for the Source Creator screen.
 */
@Stable
data class SourceCreatorState(
    // Basic Info
    val sourceName: String = "",
    val sourceUrl: String = "",
    val sourceGroup: String = "",
    val lang: String = "en",
    val comment: String = "",
    val enabled: Boolean = true,
    val header: String = "",
    
    // URLs
    val searchUrl: String = "",
    val exploreUrl: String = "",
    
    // Search Rules
    val searchBookList: String = "",
    val searchName: String = "",
    val searchAuthor: String = "",
    val searchIntro: String = "",
    val searchBookUrl: String = "",
    val searchCoverUrl: String = "",
    val searchKind: String = "",
    
    // Book Info Rules
    val bookInfoName: String = "",
    val bookInfoAuthor: String = "",
    val bookInfoIntro: String = "",
    val bookInfoCoverUrl: String = "",
    val bookInfoKind: String = "",
    val bookInfoTocUrl: String = "",
    
    // TOC Rules
    val tocChapterList: String = "",
    val tocChapterName: String = "",
    val tocChapterUrl: String = "",
    val tocNextUrl: String = "",
    val tocIsReverse: Boolean = false,
    
    // Content Rules
    val contentSelector: String = "",
    val contentNextUrl: String = "",
    val contentPurify: String = "",
    val contentReplaceRegex: String = "",
    
    // Explore Rules
    val exploreBookList: String = "",
    val exploreName: String = "",
    val exploreAuthor: String = "",
    val exploreBookUrl: String = "",
    val exploreCoverUrl: String = "",
    
    // UI State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val currentTab: Int = 0,
    val validationErrors: List<String> = emptyList(),
    val showJsonDialog: Boolean = false,
    val jsonContent: String = "",
    val snackbarMessage: String? = null
) {
    /**
     * Convert state to UserSource model.
     */
    fun toUserSource(): UserSource {
        return UserSource(
            sourceUrl = sourceUrl.trim(),
            sourceName = sourceName.trim(),
            sourceGroup = sourceGroup.trim(),
            lang = lang.trim(),
            comment = comment.trim(),
            enabled = enabled,
            header = header.trim(),
            searchUrl = searchUrl.trim(),
            exploreUrl = exploreUrl.trim(),
            ruleSearch = SearchRule(
                bookList = searchBookList.trim(),
                name = searchName.trim(),
                author = searchAuthor.trim(),
                intro = searchIntro.trim(),
                bookUrl = searchBookUrl.trim(),
                coverUrl = searchCoverUrl.trim(),
                kind = searchKind.trim()
            ),
            ruleBookInfo = BookInfoRule(
                name = bookInfoName.trim(),
                author = bookInfoAuthor.trim(),
                intro = bookInfoIntro.trim(),
                coverUrl = bookInfoCoverUrl.trim(),
                kind = bookInfoKind.trim(),
                tocUrl = bookInfoTocUrl.trim()
            ),
            ruleToc = TocRule(
                chapterList = tocChapterList.trim(),
                chapterName = tocChapterName.trim(),
                chapterUrl = tocChapterUrl.trim(),
                nextTocUrl = tocNextUrl.trim(),
                isReverse = tocIsReverse
            ),
            ruleContent = ContentRule(
                content = contentSelector.trim(),
                nextContentUrl = contentNextUrl.trim(),
                purify = contentPurify.trim(),
                replaceRegex = contentReplaceRegex.trim()
            ),
            ruleExplore = ExploreRule(
                bookList = exploreBookList.trim(),
                name = exploreName.trim(),
                author = exploreAuthor.trim(),
                bookUrl = exploreBookUrl.trim(),
                coverUrl = exploreCoverUrl.trim()
            )
        )
    }
    
    companion object {
        /**
         * Create state from existing UserSource.
         */
        fun fromUserSource(source: UserSource): SourceCreatorState {
            return SourceCreatorState(
                sourceName = source.sourceName,
                sourceUrl = source.sourceUrl,
                sourceGroup = source.sourceGroup,
                lang = source.lang,
                comment = source.comment,
                enabled = source.enabled,
                header = source.header,
                searchUrl = source.searchUrl,
                exploreUrl = source.exploreUrl,
                searchBookList = source.ruleSearch.bookList,
                searchName = source.ruleSearch.name,
                searchAuthor = source.ruleSearch.author,
                searchIntro = source.ruleSearch.intro,
                searchBookUrl = source.ruleSearch.bookUrl,
                searchCoverUrl = source.ruleSearch.coverUrl,
                searchKind = source.ruleSearch.kind,
                bookInfoName = source.ruleBookInfo.name,
                bookInfoAuthor = source.ruleBookInfo.author,
                bookInfoIntro = source.ruleBookInfo.intro,
                bookInfoCoverUrl = source.ruleBookInfo.coverUrl,
                bookInfoKind = source.ruleBookInfo.kind,
                bookInfoTocUrl = source.ruleBookInfo.tocUrl,
                tocChapterList = source.ruleToc.chapterList,
                tocChapterName = source.ruleToc.chapterName,
                tocChapterUrl = source.ruleToc.chapterUrl,
                tocNextUrl = source.ruleToc.nextTocUrl,
                tocIsReverse = source.ruleToc.isReverse,
                contentSelector = source.ruleContent.content,
                contentNextUrl = source.ruleContent.nextContentUrl,
                contentPurify = source.ruleContent.purify,
                contentReplaceRegex = source.ruleContent.replaceRegex,
                exploreBookList = source.ruleExplore.bookList,
                exploreName = source.ruleExplore.name,
                exploreAuthor = source.ruleExplore.author,
                exploreBookUrl = source.ruleExplore.bookUrl,
                exploreCoverUrl = source.ruleExplore.coverUrl,
                isEditing = true
            )
        }
    }
}

/**
 * Tabs for the source creator screen.
 */
enum class SourceCreatorTab(val title: String) {
    BASIC("Basic"),
    SEARCH("Search"),
    BOOK_INFO("Book Info"),
    TOC("Chapters"),
    CONTENT("Content"),
    EXPLORE("Explore")
}
