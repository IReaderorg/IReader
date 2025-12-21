package ireader.presentation.ui.sourcecreator.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.sourcecreator.SourceCreatorState
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Basic Information")
        
        RuleTextField(
            value = state.sourceName,
            onValueChange = onSourceNameChange,
            label = localizeHelper.localize(Res.string.source_name),
            placeholder = "My Novel Source"
        )
        
        RuleTextField(
            value = state.sourceUrl,
            onValueChange = onSourceUrlChange,
            label = localizeHelper.localize(Res.string.source_url),
            placeholder = "https://example.com"
        )
        
        RuleTextField(
            value = state.sourceGroup,
            onValueChange = onSourceGroupChange,
            label = localizeHelper.localize(Res.string.group),
            placeholder = "English, Chinese, etc."
        )
        
        RuleTextField(
            value = state.lang,
            onValueChange = onLangChange,
            label = localizeHelper.localize(Res.string.language_code),
            placeholder = "en, zh, ru, etc."
        )
        
        RuleTextField(
            value = state.comment,
            onValueChange = onCommentChange,
            label = localizeHelper.localize(Res.string.commentdescription),
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
            label = localizeHelper.localize(Res.string.search_url),
            placeholder = "https://example.com/search?q={{key}}&page={{page}}",
            helperText = "Use {{key}} for search keyword, {{page}} for page number"
        )
        
        RuleTextField(
            value = state.exploreUrl,
            onValueChange = onExploreUrlChange,
            label = localizeHelper.localize(Res.string.explore_url),
            placeholder = "https://example.com/latest?page={{page}}",
            helperText = "URL for browsing/discovery"
        )
        
        Divider()
        SectionTitle("Custom Headers (Optional)")
        
        RuleTextField(
            value = state.header,
            onValueChange = onHeaderChange,
            label = localizeHelper.localize(Res.string.headers_json),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Search Result Rules")
        HelpText(localizeHelper.localize(Res.string.css_selectors_for_parsing_search))
        
        RuleTextField(
            value = state.searchBookList,
            onValueChange = onBookListChange,
            label = localizeHelper.localize(Res.string.book_list_selector),
            placeholder = "div.search-result, ul.book-list li",
            helperText = "Selector for each book item in results"
        )
        
        RuleTextField(
            value = state.searchName,
            onValueChange = onNameChange,
            label = localizeHelper.localize(Res.string.name_selector),
            placeholder = "h3.title, a.book-name",
            helperText = "Selector for book title within each item"
        )
        
        RuleTextField(
            value = state.searchBookUrl,
            onValueChange = onBookUrlChange,
            label = localizeHelper.localize(Res.string.book_url_selector),
            placeholder = "a@href, a.book-link@href",
            helperText = "Selector for book detail page URL"
        )
        
        RuleTextField(
            value = state.searchAuthor,
            onValueChange = onAuthorChange,
            label = localizeHelper.localize(Res.string.author_selector),
            placeholder = "span.author, div.info .author"
        )
        
        RuleTextField(
            value = state.searchCoverUrl,
            onValueChange = onCoverUrlChange,
            label = localizeHelper.localize(Res.string.cover_url_selector),
            placeholder = "img@src, img.cover@data-src"
        )
        
        RuleTextField(
            value = state.searchIntro,
            onValueChange = onIntroChange,
            label = localizeHelper.localize(Res.string.intro_selector),
            placeholder = "p.description, div.intro"
        )
        
        RuleTextField(
            value = state.searchKind,
            onValueChange = onKindChange,
            label = localizeHelper.localize(Res.string.genrekind_selector),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Book Detail Page Rules")
        HelpText(localizeHelper.localize(Res.string.selectors_for_the_book_detailinfo_page))
        
        RuleTextField(
            value = state.bookInfoName,
            onValueChange = onNameChange,
            label = localizeHelper.localize(Res.string.name_selector_1),
            placeholder = "h1.book-title"
        )
        
        RuleTextField(
            value = state.bookInfoAuthor,
            onValueChange = onAuthorChange,
            label = localizeHelper.localize(Res.string.author_selector),
            placeholder = "span.author, div.info .author"
        )
        
        RuleTextField(
            value = state.bookInfoIntro,
            onValueChange = onIntroChange,
            label = localizeHelper.localize(Res.string.description_selector),
            placeholder = "div.synopsis, div.description"
        )
        
        RuleTextField(
            value = state.bookInfoCoverUrl,
            onValueChange = onCoverUrlChange,
            label = localizeHelper.localize(Res.string.cover_url_selector),
            placeholder = "div.cover img@src"
        )
        
        RuleTextField(
            value = state.bookInfoKind,
            onValueChange = onKindChange,
            label = localizeHelper.localize(Res.string.genre_selector),
            placeholder = "div.genres a, span.category"
        )
        
        RuleTextField(
            value = state.bookInfoTocUrl,
            onValueChange = onTocUrlChange,
            label = localizeHelper.localize(Res.string.toc_url_selector),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Chapter List Rules")
        HelpText(localizeHelper.localize(Res.string.selectors_for_parsing_the_table_of_contents))
        
        RuleTextField(
            value = state.tocChapterList,
            onValueChange = onChapterListChange,
            label = localizeHelper.localize(Res.string.chapter_list_selector),
            placeholder = "ul.chapter-list li, div.chapters a",
            helperText = "Selector for each chapter item"
        )
        
        RuleTextField(
            value = state.tocChapterName,
            onValueChange = onChapterNameChange,
            label = localizeHelper.localize(Res.string.chapter_name_selector),
            placeholder = "a, span.title",
            helperText = "Selector for chapter title within each item"
        )
        
        RuleTextField(
            value = state.tocChapterUrl,
            onValueChange = onChapterUrlChange,
            label = localizeHelper.localize(Res.string.chapter_url_selector),
            placeholder = "a@href",
            helperText = "Selector for chapter page URL"
        )
        
        RuleTextField(
            value = state.tocNextUrl,
            onValueChange = onNextUrlChange,
            label = localizeHelper.localize(Res.string.next_page_selector),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Chapter Content Rules")
        HelpText(localizeHelper.localize(Res.string.selectors_for_parsing_chapter_content))
        
        RuleTextField(
            value = state.contentSelector,
            onValueChange = onContentSelectorChange,
            label = localizeHelper.localize(Res.string.content_selector),
            placeholder = "div.chapter-content, div#content",
            helperText = "Selector for the main text content"
        )
        
        RuleTextField(
            value = state.contentNextUrl,
            onValueChange = onNextUrlChange,
            label = localizeHelper.localize(Res.string.next_page_selector),
            placeholder = "a.next-part@href",
            helperText = "For multi-page chapters"
        )
        
        RuleTextField(
            value = state.contentPurify,
            onValueChange = onPurifyChange,
            label = localizeHelper.localize(Res.string.remove_selectors),
            placeholder = "div.ads, script, div.social",
            helperText = "Comma-separated selectors to remove"
        )
        
        RuleTextField(
            value = state.contentReplaceRegex,
            onValueChange = onReplaceRegexChange,
            label = localizeHelper.localize(Res.string.replace_regex),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Explore/Browse Rules")
        HelpText(localizeHelper.localize(Res.string.selectors_for_the_explorelatest_page))
        
        RuleTextField(
            value = state.exploreBookList,
            onValueChange = onBookListChange,
            label = localizeHelper.localize(Res.string.book_list_selector_1),
            placeholder = "div.novel-list article"
        )
        
        RuleTextField(
            value = state.exploreName,
            onValueChange = onNameChange,
            label = localizeHelper.localize(Res.string.name_selector_1),
            placeholder = "h2.title"
        )
        
        RuleTextField(
            value = state.exploreBookUrl,
            onValueChange = onBookUrlChange,
            label = localizeHelper.localize(Res.string.book_url_selector_1),
            placeholder = "a@href"
        )
        
        RuleTextField(
            value = state.exploreAuthor,
            onValueChange = onAuthorChange,
            label = localizeHelper.localize(Res.string.author_selector),
            placeholder = "span.author"
        )
        
        RuleTextField(
            value = state.exploreCoverUrl,
            onValueChange = onCoverUrlChange,
            label = localizeHelper.localize(Res.string.cover_url_selector),
            placeholder = "img@src"
        )
    }
}
