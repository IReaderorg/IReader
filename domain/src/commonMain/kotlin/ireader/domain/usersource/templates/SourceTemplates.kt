package ireader.domain.usersource.templates

import ireader.domain.usersource.model.*

/**
 * Pre-built source templates for common website patterns.
 * Users can select a template and only fill in the URL and adjust selectors.
 */
object SourceTemplates {
    
    /**
     * Template metadata for display in UI.
     */
    data class SourceTemplate(
        val id: String,
        val name: String,
        val description: String,
        val category: TemplateCategory,
        val source: UserSource
    )
    
    enum class TemplateCategory(val displayName: String) {
        WORDPRESS("WordPress Sites"),
        MADARA("Madara Theme"),
        CUSTOM("Custom/Generic"),
        CHINESE("Chinese Sites"),
        KOREAN("Korean Sites")
    }
    
    /**
     * WordPress-based novel sites (common pattern).
     */
    val wordpressTemplate = SourceTemplate(
        id = "wordpress",
        name = "WordPress Novel",
        description = "For WordPress sites with standard novel plugins",
        category = TemplateCategory.WORDPRESS,
        source = UserSource(
            sourceName = "",
            sourceUrl = "",
            sourceGroup = "WordPress",
            lang = "en",
            searchUrl = "{{baseUrl}}/?s={{key}}&post_type=wp-manga",
            ruleSearch = SearchRule(
                bookList = "div.c-tabs-item__content",
                name = "div.post-title h3 a",
                author = "div.mg_author a",
                bookUrl = "div.post-title h3 a@href",
                coverUrl = "div.tab-thumb img@src",
                intro = "div.post-content p"
            ),
            ruleBookInfo = BookInfoRule(
                name = "div.post-title h1",
                author = "div.author-content a",
                intro = "div.description-summary div.summary__content p",
                coverUrl = "div.summary_image img@src",
                kind = "div.genres-content a"
            ),
            ruleToc = TocRule(
                chapterList = "li.wp-manga-chapter",
                chapterName = "a",
                chapterUrl = "a@href"
            ),
            ruleContent = ContentRule(
                content = "div.text-left, div.reading-content",
                purify = "div.ads, script, div.code-block"
            )
        )
    )
    
    /**
     * Madara theme (very common for manga/novel sites).
     */
    val madaraTemplate = SourceTemplate(
        id = "madara",
        name = "Madara Theme",
        description = "For sites using Madara WordPress theme",
        category = TemplateCategory.MADARA,
        source = UserSource(
            sourceName = "",
            sourceUrl = "",
            sourceGroup = "Madara",
            lang = "en",
            searchUrl = "{{baseUrl}}/?s={{key}}&post_type=wp-manga",
            exploreUrl = "{{baseUrl}}/manga/?m_orderby=latest&page={{page}}",
            ruleSearch = SearchRule(
                bookList = "div.row.c-tabs-item__content",
                name = "div.post-title h3 a, div.post-title h4 a",
                author = "div.mg_author a, span.author-content a",
                bookUrl = "div.post-title a@href",
                coverUrl = "img.img-responsive@data-src||img.img-responsive@src",
                intro = "div.post-content div.summary-content p"
            ),
            ruleBookInfo = BookInfoRule(
                name = "div.post-title h1",
                author = "div.author-content a",
                intro = "div.summary__content p",
                coverUrl = "div.summary_image img@data-src||div.summary_image img@src",
                kind = "div.genres-content a"
            ),
            ruleToc = TocRule(
                chapterList = "li.wp-manga-chapter",
                chapterName = "a",
                chapterUrl = "a@href",
                isReverse = true
            ),
            ruleContent = ContentRule(
                content = "div.reading-content div.text-left",
                purify = "div.ads, script, div.code-block, div.adsbox"
            )
        )
    )
    
    /**
     * Generic/simple HTML site.
     */
    val genericTemplate = SourceTemplate(
        id = "generic",
        name = "Generic Site",
        description = "Basic template - customize all selectors",
        category = TemplateCategory.CUSTOM,
        source = UserSource(
            sourceName = "",
            sourceUrl = "",
            lang = "en",
            searchUrl = "{{baseUrl}}/search?q={{key}}&page={{page}}",
            ruleSearch = SearchRule(
                bookList = "div.search-result, ul.book-list li",
                name = "h3.title, a.book-name",
                author = "span.author",
                bookUrl = "a@href",
                coverUrl = "img@src"
            ),
            ruleBookInfo = BookInfoRule(
                name = "h1",
                author = "span.author",
                intro = "div.description, div.synopsis",
                coverUrl = "img.cover@src"
            ),
            ruleToc = TocRule(
                chapterList = "ul.chapter-list li, div.chapters a",
                chapterName = "a",
                chapterUrl = "a@href"
            ),
            ruleContent = ContentRule(
                content = "div.content, div.chapter-content, article"
            )
        )
    )
    
    /**
     * Chinese novel site pattern.
     */
    val chineseTemplate = SourceTemplate(
        id = "chinese",
        name = "Chinese Novel Site",
        description = "Common pattern for Chinese novel websites",
        category = TemplateCategory.CHINESE,
        source = UserSource(
            sourceName = "",
            sourceUrl = "",
            sourceGroup = "中文",
            lang = "zh",
            searchUrl = "{{baseUrl}}/search.php?searchkey={{key}}",
            ruleSearch = SearchRule(
                bookList = "div.result-list div.result-item, ul.list li",
                name = "a.bookname, h3 a",
                author = "span.author, a.author",
                bookUrl = "a@href",
                coverUrl = "img@src||img@data-src"
            ),
            ruleBookInfo = BookInfoRule(
                name = "div.book-info h1, h1.title",
                author = "span.author a, div.info span:contains(作者)",
                intro = "div.intro, div.description",
                coverUrl = "div.book-cover img@src"
            ),
            ruleToc = TocRule(
                chapterList = "div.chapter-list dd, ul.chapter-list li",
                chapterName = "a",
                chapterUrl = "a@href"
            ),
            ruleContent = ContentRule(
                content = "div#content, div.content",
                purify = "div.ad, script, p:contains(本章未完)"
            )
        )
    )
    
    /**
     * Korean novel site pattern.
     */
    val koreanTemplate = SourceTemplate(
        id = "korean",
        name = "Korean Novel Site",
        description = "Common pattern for Korean novel websites",
        category = TemplateCategory.KOREAN,
        source = UserSource(
            sourceName = "",
            sourceUrl = "",
            sourceGroup = "한국어",
            lang = "ko",
            searchUrl = "{{baseUrl}}/search?keyword={{key}}",
            ruleSearch = SearchRule(
                bookList = "div.novel-list div.novel-item",
                name = "h3.title a",
                author = "span.author",
                bookUrl = "a@href",
                coverUrl = "img@src"
            ),
            ruleBookInfo = BookInfoRule(
                name = "h1.novel-title",
                author = "span.author",
                intro = "div.synopsis",
                coverUrl = "div.cover img@src"
            ),
            ruleToc = TocRule(
                chapterList = "ul.chapter-list li",
                chapterName = "a",
                chapterUrl = "a@href"
            ),
            ruleContent = ContentRule(
                content = "div.chapter-content"
            )
        )
    )
    
    /**
     * All available templates.
     */
    val allTemplates = listOf(
        genericTemplate,
        wordpressTemplate,
        madaraTemplate,
        chineseTemplate,
        koreanTemplate
    )
    
    /**
     * Get templates by category.
     */
    fun getByCategory(category: TemplateCategory): List<SourceTemplate> {
        return allTemplates.filter { it.category == category }
    }
    
    /**
     * Get template by ID.
     */
    fun getById(id: String): SourceTemplate? {
        return allTemplates.find { it.id == id }
    }
    
    /**
     * Apply template to create a new source with user's URL.
     */
    fun applyTemplate(template: SourceTemplate, sourceName: String, sourceUrl: String): UserSource {
        return template.source.copy(
            sourceName = sourceName,
            sourceUrl = sourceUrl.trimEnd('/')
        )
    }
}
