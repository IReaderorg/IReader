package ireader.presentation.ui.sourcecreator.wizard

import androidx.compose.runtime.Stable
import ireader.domain.usersource.model.UserSource
import ireader.domain.usersource.templates.SourceTemplates

/**
 * State for the step-by-step source creation wizard.
 */
@Stable
data class SourceWizardState(
    val currentStep: WizardStep = WizardStep.CHOOSE_METHOD,
    val selectedTemplate: SourceTemplates.SourceTemplate? = null,
    
    // Basic info
    val sourceName: String = "",
    val sourceUrl: String = "",
    val sourceGroup: String = "",
    val lang: String = "en",
    
    // Test results
    val testSearchResult: TestResult? = null,
    val testChaptersResult: TestResult? = null,
    val testContentResult: TestResult? = null,
    
    // Current source being built
    val currentSource: UserSource = UserSource(),
    
    // UI state
    val isLoading: Boolean = false,
    val isTesting: Boolean = false,
    val errorMessage: String? = null,
    val showTemplateDialog: Boolean = false
)

/**
 * Wizard steps for guided source creation.
 */
enum class WizardStep(val title: String, val description: String) {
    CHOOSE_METHOD(
        "Choose Method",
        "Start from a template or create from scratch"
    ),
    BASIC_INFO(
        "Basic Info",
        "Enter the website name and URL"
    ),
    SEARCH_RULES(
        "Search Setup",
        "Configure how to search for books"
    ),
    BOOK_INFO_RULES(
        "Book Details",
        "Configure how to get book information"
    ),
    CHAPTER_RULES(
        "Chapter List",
        "Configure how to get the chapter list"
    ),
    CONTENT_RULES(
        "Content",
        "Configure how to read chapter content"
    ),
    TEST_AND_SAVE(
        "Test & Save",
        "Test your source and save it"
    )
}

/**
 * Result of testing a source rule.
 */
@Stable
data class TestResult(
    val success: Boolean,
    val message: String,
    val sampleData: List<String> = emptyList()
)

/**
 * Common selector suggestions for different elements.
 */
object SelectorSuggestions {
    
    val bookListSelectors = listOf(
        "div.search-result" to "Search result container",
        "ul.book-list li" to "Book list items",
        "div.novel-item" to "Novel items",
        "article.post" to "WordPress posts",
        "div.c-tabs-item__content" to "Madara theme"
    )
    
    val titleSelectors = listOf(
        "h1" to "Main heading",
        "h2.title" to "Title heading",
        "h3.title" to "Title in h3",
        "a.book-name" to "Book name link",
        "div.post-title a" to "Post title link"
    )
    
    val authorSelectors = listOf(
        "span.author" to "Author span",
        "a.author" to "Author link",
        "div.author-content a" to "Author content",
        "span:contains(Author)" to "Contains 'Author'"
    )
    
    val coverSelectors = listOf(
        "img@src" to "Image source",
        "img@data-src" to "Lazy-loaded image",
        "img.cover@src" to "Cover image",
        "div.cover img@src" to "Cover container image"
    )
    
    val linkSelectors = listOf(
        "a@href" to "Link href",
        "a.read@href" to "Read link",
        "a.chapter@href" to "Chapter link"
    )
    
    val contentSelectors = listOf(
        "div.content" to "Content div",
        "div.chapter-content" to "Chapter content",
        "div#content" to "Content by ID",
        "article" to "Article element",
        "div.text-left" to "Text container"
    )
    
    val chapterListSelectors = listOf(
        "ul.chapter-list li" to "Chapter list items",
        "div.chapters a" to "Chapter links",
        "li.wp-manga-chapter" to "Madara chapters",
        "dd a" to "Definition list chapters"
    )
}
