package ireader.presentation.ui.sourcecreator.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.sourcecreator.SourceCreatorState

/**
 * Basic info tab for source creator.
 */
@Composable
fun BasicInfoTab(
    state: SourceCreatorState,
    onSourceNameChange: (String) -> Unit,
    onSourceUrlChange: (String) -> Unit,
    onSourceGroupChange: (String) -> Unit,
    onLangChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onHeaderChange: (String) -> Unit,
    onSearchUrlChange: (String) -> Unit,
    onExploreUrlChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Basic Information")
        
        RuleTextField(
            value = state.sourceName,
            onValueChange = onSourceNameChange,
            label = "Source Name *",
            placeholder = "My Novel Source"
        )
        
        RuleTextField(
            value = state.sourceUrl,
            onValueChange = onSourceUrlChange,
            label = "Source URL *",
            placeholder = "https://example.com"
        )
        
        RuleTextField(
            value = state.sourceGroup,
            onValueChange = onSourceGroupChange,
            label = "Group",
            placeholder = "English, Chinese, etc."
        )
        
        RuleTextField(
            value = state.lang,
            onValueChange = onLangChange,
            label = "Language Code",
            placeholder = "en, zh, ru, etc."
        )
        
        RuleTextField(
            value = state.comment,
            onValueChange = onCommentChange,
            label = "Comment/Description",
            placeholder = "Description of this source",
            singleLine = false,
            maxLines = 3
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enabled", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = state.enabled,
                onCheckedChange = onEnabledChange
            )
        }
        
        Divider()
        SectionTitle("URLs")
        
        RuleTextField(
            value = state.searchUrl,
            onValueChange = onSearchUrlChange,
            label = "Search URL",
            placeholder = "https://example.com/search?q={{key}}&page={{page}}",
            helperText = "Use {{key}} for search keyword, {{page}} for page number"
        )
        
        RuleTextField(
            value = state.exploreUrl,
            onValueChange = onExploreUrlChange,
            label = "Explore URL",
            placeholder = "https://example.com/latest?page={{page}}",
            helperText = "URL for browsing/discovery"
        )
        
        Divider()
        SectionTitle("Custom Headers (Optional)")
        
        RuleTextField(
            value = state.header,
            onValueChange = onHeaderChange,
            label = "Headers (JSON)",
            placeholder = """{"User-Agent": "Mozilla/5.0"}""",
            singleLine = false,
            maxLines = 4
        )
    }
}

/**
 * Search rules tab.
 */
@Composable
fun SearchRulesTab(
    state: SourceCreatorState,
    onBookListChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onIntroChange: (String) -> Unit,
    onBookUrlChange: (String) -> Unit,
    onCoverUrlChange: (String) -> Unit,
    onKindChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Search Result Rules")
        HelpText("CSS selectors for parsing search results. Use @attr for attributes (e.g., a@href)")
        
        RuleTextField(
            value = state.searchBookList,
            onValueChange = onBookListChange,
            label = "Book List Selector *",
            placeholder = "div.search-result, ul.book-list li",
            helperText = "Selector for each book item in results"
        )
        
        RuleTextField(
            value = state.searchName,
            onValueChange = onNameChange,
            label = "Name Selector *",
            placeholder = "h3.title, a.book-name",
            helperText = "Selector for book title within each item"
        )
        
        RuleTextField(
            value = state.searchBookUrl,
            onValueChange = onBookUrlChange,
            label = "Book URL Selector *",
            placeholder = "a@href, a.book-link@href",
            helperText = "Selector for book detail page URL"
        )
        
        RuleTextField(
            value = state.searchAuthor,
            onValueChange = onAuthorChange,
            label = "Author Selector",
            placeholder = "span.author, div.info .author"
        )
        
        RuleTextField(
            value = state.searchCoverUrl,
            onValueChange = onCoverUrlChange,
            label = "Cover URL Selector",
            placeholder = "img@src, img.cover@data-src"
        )
        
        RuleTextField(
            value = state.searchIntro,
            onValueChange = onIntroChange,
            label = "Intro Selector",
            placeholder = "p.description, div.intro"
        )
        
        RuleTextField(
            value = state.searchKind,
            onValueChange = onKindChange,
            label = "Genre/Kind Selector",
            placeholder = "span.genre, div.tags a"
        )
    }
}

/**
 * Book info rules tab.
 */
@Composable
fun BookInfoRulesTab(
    state: SourceCreatorState,
    onNameChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onIntroChange: (String) -> Unit,
    onCoverUrlChange: (String) -> Unit,
    onKindChange: (String) -> Unit,
    onTocUrlChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Book Detail Page Rules")
        HelpText("Selectors for the book detail/info page")
        
        RuleTextField(
            value = state.bookInfoName,
            onValueChange = onNameChange,
            label = "Name Selector",
            placeholder = "h1.book-title"
        )
        
        RuleTextField(
            value = state.bookInfoAuthor,
            onValueChange = onAuthorChange,
            label = "Author Selector",
            placeholder = "span.author, div.info .author"
        )
        
        RuleTextField(
            value = state.bookInfoIntro,
            onValueChange = onIntroChange,
            label = "Description Selector",
            placeholder = "div.synopsis, div.description"
        )
        
        RuleTextField(
            value = state.bookInfoCoverUrl,
            onValueChange = onCoverUrlChange,
            label = "Cover URL Selector",
            placeholder = "div.cover img@src"
        )
        
        RuleTextField(
            value = state.bookInfoKind,
            onValueChange = onKindChange,
            label = "Genre Selector",
            placeholder = "div.genres a, span.category"
        )
        
        RuleTextField(
            value = state.bookInfoTocUrl,
            onValueChange = onTocUrlChange,
            label = "TOC URL Selector",
            placeholder = "a.read-btn@href",
            helperText = "If chapter list is on a different page"
        )
    }
}

/**
 * TOC rules tab.
 */
@Composable
fun TocRulesTab(
    state: SourceCreatorState,
    onChapterListChange: (String) -> Unit,
    onChapterNameChange: (String) -> Unit,
    onChapterUrlChange: (String) -> Unit,
    onNextUrlChange: (String) -> Unit,
    onIsReverseChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Chapter List Rules")
        HelpText("Selectors for parsing the table of contents")
        
        RuleTextField(
            value = state.tocChapterList,
            onValueChange = onChapterListChange,
            label = "Chapter List Selector *",
            placeholder = "ul.chapter-list li, div.chapters a",
            helperText = "Selector for each chapter item"
        )
        
        RuleTextField(
            value = state.tocChapterName,
            onValueChange = onChapterNameChange,
            label = "Chapter Name Selector *",
            placeholder = "a, span.title",
            helperText = "Selector for chapter title within each item"
        )
        
        RuleTextField(
            value = state.tocChapterUrl,
            onValueChange = onChapterUrlChange,
            label = "Chapter URL Selector *",
            placeholder = "a@href",
            helperText = "Selector for chapter page URL"
        )
        
        RuleTextField(
            value = state.tocNextUrl,
            onValueChange = onNextUrlChange,
            label = "Next Page Selector",
            placeholder = "a.next-page@href",
            helperText = "For paginated chapter lists"
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Reverse Chapter Order", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = state.tocIsReverse,
                onCheckedChange = onIsReverseChange
            )
        }
    }
}

/**
 * Content rules tab.
 */
@Composable
fun ContentRulesTab(
    state: SourceCreatorState,
    onContentSelectorChange: (String) -> Unit,
    onNextUrlChange: (String) -> Unit,
    onPurifyChange: (String) -> Unit,
    onReplaceRegexChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Chapter Content Rules")
        HelpText("Selectors for parsing chapter content")
        
        RuleTextField(
            value = state.contentSelector,
            onValueChange = onContentSelectorChange,
            label = "Content Selector *",
            placeholder = "div.chapter-content, div#content",
            helperText = "Selector for the main text content"
        )
        
        RuleTextField(
            value = state.contentNextUrl,
            onValueChange = onNextUrlChange,
            label = "Next Page Selector",
            placeholder = "a.next-part@href",
            helperText = "For multi-page chapters"
        )
        
        RuleTextField(
            value = state.contentPurify,
            onValueChange = onPurifyChange,
            label = "Remove Selectors",
            placeholder = "div.ads, script, div.social",
            helperText = "Comma-separated selectors to remove"
        )
        
        RuleTextField(
            value = state.contentReplaceRegex,
            onValueChange = onReplaceRegexChange,
            label = "Replace Regex",
            placeholder = "pattern##replacement",
            helperText = "Regex pattern to clean content"
        )
    }
}

/**
 * Explore rules tab.
 */
@Composable
fun ExploreRulesTab(
    state: SourceCreatorState,
    onBookListChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onBookUrlChange: (String) -> Unit,
    onCoverUrlChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Explore/Browse Rules")
        HelpText("Selectors for the explore/latest page (optional, uses search rules if empty)")
        
        RuleTextField(
            value = state.exploreBookList,
            onValueChange = onBookListChange,
            label = "Book List Selector",
            placeholder = "div.novel-list article"
        )
        
        RuleTextField(
            value = state.exploreName,
            onValueChange = onNameChange,
            label = "Name Selector",
            placeholder = "h2.title"
        )
        
        RuleTextField(
            value = state.exploreBookUrl,
            onValueChange = onBookUrlChange,
            label = "Book URL Selector",
            placeholder = "a@href"
        )
        
        RuleTextField(
            value = state.exploreAuthor,
            onValueChange = onAuthorChange,
            label = "Author Selector",
            placeholder = "span.author"
        )
        
        RuleTextField(
            value = state.exploreCoverUrl,
            onValueChange = onCoverUrlChange,
            label = "Cover URL Selector",
            placeholder = "img@src"
        )
    }
}
