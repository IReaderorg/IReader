package ireader.domain.usersource.help

/**
 * Help content and tutorials for source creation.
 * Provides explanations, examples, and tips for non-technical users.
 */
object SourceCreatorHelp {
    
    /**
     * Help topic with title, content, and examples.
     */
    data class HelpTopic(
        val id: String,
        val title: String,
        val shortDescription: String,
        val content: String,
        val examples: List<HelpExample> = emptyList(),
        val tips: List<String> = emptyList(),
        val relatedTopics: List<String> = emptyList()
    )
    
    data class HelpExample(
        val description: String,
        val code: String,
        val result: String? = null
    )
    
    /**
     * Interactive tutorial step.
     */
    data class TutorialStep(
        val id: String,
        val title: String,
        val instruction: String,
        val targetField: String? = null,
        val exampleValue: String? = null,
        val validation: ((String) -> Boolean)? = null
    )
    
    // ==================== Help Topics ====================
    
    val whatIsSelector = HelpTopic(
        id = "what_is_selector",
        title = "What is a CSS Selector?",
        shortDescription = "Learn the basics of CSS selectors",
        content = """
            A CSS selector is like an address that tells the app where to find information on a webpage.
            
            Think of a webpage like a house:
            • The house has rooms (like <div>, <section>)
            • Rooms have furniture (like <p>, <span>, <a>)
            • Furniture has labels (like class="title", id="content")
            
            A selector is the directions to find specific furniture in the house.
        """.trimIndent(),
        examples = listOf(
            HelpExample(
                "Find all paragraphs",
                "p",
                "Selects all <p> elements"
            ),
            HelpExample(
                "Find element with specific class",
                "div.title",
                "Selects <div class=\"title\">"
            ),
            HelpExample(
                "Find element with specific ID",
                "#content",
                "Selects element with id=\"content\""
            )
        ),
        tips = listOf(
            "Use your browser's 'Inspect Element' to see the HTML structure",
            "Right-click on any element and select 'Copy selector' for a quick start",
            "Start simple and add more specificity if needed"
        ),
        relatedTopics = listOf("common_selectors", "attribute_extraction")
    )
    
    val commonSelectors = HelpTopic(
        id = "common_selectors",
        title = "Common Selector Patterns",
        shortDescription = "Most frequently used selector patterns",
        content = """
            Here are the most common patterns you'll use:
            
            1. Tag selector: Just the HTML tag name
            2. Class selector: A dot (.) followed by the class name
            3. ID selector: A hash (#) followed by the ID
            4. Descendant selector: Space between selectors
            5. Child selector: Greater than (>) between selectors
        """.trimIndent(),
        examples = listOf(
            HelpExample("Tag", "h1", "All h1 headings"),
            HelpExample("Class", ".book-title", "Elements with class 'book-title'"),
            HelpExample("ID", "#main-content", "Element with id 'main-content'"),
            HelpExample("Descendant", "div.books a", "Links inside div with class 'books'"),
            HelpExample("Child", "ul > li", "Direct li children of ul"),
            HelpExample("Multiple classes", "div.book.featured", "div with both classes"),
            HelpExample("Attribute", "a[href]", "Links with href attribute")
        ),
        tips = listOf(
            "Classes are more reliable than tag names",
            "IDs are unique - use them when available",
            "Avoid overly specific selectors that might break"
        )
    )
    
    val attributeExtraction = HelpTopic(
        id = "attribute_extraction",
        title = "Extracting Attributes",
        shortDescription = "How to get links, images, and other attributes",
        content = """
            Sometimes you need more than just the text - you need the link URL or image source.
            
            Use @ followed by the attribute name:
            • selector@href - Get the link URL
            • selector@src - Get the image source
            • selector@data-src - Get lazy-loaded image source
            • selector@text - Get the text content (default)
        """.trimIndent(),
        examples = listOf(
            HelpExample(
                "Get link URL",
                "a.book-link@href",
                "/books/123"
            ),
            HelpExample(
                "Get image source",
                "img.cover@src",
                "https://example.com/cover.jpg"
            ),
            HelpExample(
                "Get lazy-loaded image",
                "img@data-src",
                "https://example.com/lazy-image.jpg"
            ),
            HelpExample(
                "Get custom attribute",
                "div@data-book-id",
                "12345"
            )
        ),
        tips = listOf(
            "Most images use 'src', but lazy-loaded images often use 'data-src'",
            "Links always use 'href' for the URL",
            "Use || for fallback: img@data-src||img@src"
        )
    )

    
    val fallbackRules = HelpTopic(
        id = "fallback_rules",
        title = "Fallback Rules",
        shortDescription = "Try multiple selectors until one works",
        content = """
            Different websites use different HTML structures. Fallback rules let you try multiple selectors.
            
            Use || (double pipe) to separate fallback options:
            The app will try each selector in order and use the first one that finds something.
        """.trimIndent(),
        examples = listOf(
            HelpExample(
                "Try multiple title selectors",
                "h1.title||h2.title||div.book-name",
                "Tries h1.title first, then h2.title, then div.book-name"
            ),
            HelpExample(
                "Image with fallback",
                "img@data-src||img@src",
                "Tries data-src first (lazy loading), falls back to src"
            )
        ),
        tips = listOf(
            "Put the most specific selector first",
            "Use fallbacks for sites that might change their HTML",
            "Test each fallback option individually first"
        )
    )
    
    val searchUrlHelp = HelpTopic(
        id = "search_url",
        title = "Search URL Template",
        shortDescription = "How to configure the search URL",
        content = """
            The search URL tells the app where to search for books.
            
            Use placeholders that get replaced:
            • {{baseUrl}} - The website's main URL
            • {{key}} - The search term the user types
            • {{page}} - The page number (for pagination)
            • {{pageIndex}} - Page number starting from 0
        """.trimIndent(),
        examples = listOf(
            HelpExample(
                "Simple search",
                "{{baseUrl}}/search?q={{key}}",
                "https://example.com/search?q=harry+potter"
            ),
            HelpExample(
                "With pagination",
                "{{baseUrl}}/search?q={{key}}&page={{page}}",
                "https://example.com/search?q=harry+potter&page=2"
            ),
            HelpExample(
                "WordPress style",
                "{{baseUrl}}/?s={{key}}&post_type=wp-manga",
                "https://example.com/?s=harry+potter&post_type=wp-manga"
            )
        ),
        tips = listOf(
            "Search on the website manually and look at the URL",
            "The search term in the URL is usually after 'q=', 's=', or 'keyword='",
            "{{key}} will be automatically URL-encoded"
        )
    )
    
    val bookListHelp = HelpTopic(
        id = "book_list",
        title = "Book List Selector",
        shortDescription = "Finding books in search results",
        content = """
            The book list selector finds each book item in search results.
            
            Look for a repeating pattern - each book should be in a similar container.
            The selector should match ALL book items, not just one.
        """.trimIndent(),
        examples = listOf(
            HelpExample(
                "List items",
                "ul.book-list li",
                "Each <li> in the book list"
            ),
            HelpExample(
                "Div containers",
                "div.search-result",
                "Each div with class 'search-result'"
            ),
            HelpExample(
                "Madara theme",
                "div.c-tabs-item__content",
                "Common WordPress theme pattern"
            )
        ),
        tips = listOf(
            "Count the results - the selector should match all books on the page",
            "Look for classes like 'item', 'result', 'book', 'novel'",
            "The container usually has the title, author, and cover inside it"
        )
    )
    
    val chapterListHelp = HelpTopic(
        id = "chapter_list",
        title = "Chapter List Selector",
        shortDescription = "Finding chapters on a book page",
        content = """
            The chapter list selector finds each chapter link.
            
            Chapters are usually in a list (<ul>, <ol>) or a series of links.
            Each chapter should have a name and a URL.
        """.trimIndent(),
        examples = listOf(
            HelpExample(
                "Unordered list",
                "ul.chapter-list li",
                "Each chapter in the list"
            ),
            HelpExample(
                "WordPress/Madara",
                "li.wp-manga-chapter",
                "Madara theme chapters"
            ),
            HelpExample(
                "Definition list",
                "dd a",
                "Links in definition list"
            )
        ),
        tips = listOf(
            "Chapters might be in reverse order (newest first)",
            "Check the 'Reverse' option if chapters are backwards",
            "Some sites paginate chapters - look for 'next page' links"
        )
    )
    
    val contentHelp = HelpTopic(
        id = "content",
        title = "Content Selector",
        shortDescription = "Finding the chapter text",
        content = """
            The content selector finds the main text of each chapter.
            
            This is usually a large div containing paragraphs of text.
            You may need to remove ads and other unwanted elements.
        """.trimIndent(),
        examples = listOf(
            HelpExample(
                "By ID",
                "div#content",
                "Div with id 'content'"
            ),
            HelpExample(
                "By class",
                "div.chapter-content",
                "Div with class 'chapter-content'"
            ),
            HelpExample(
                "Article element",
                "article",
                "The article element"
            )
        ),
        tips = listOf(
            "The content div usually has the most text on the page",
            "Use 'Remove Elements' to clean up ads: div.ads, script",
            "Test with a chapter that has special characters"
        )
    )
    
    val purifyHelp = HelpTopic(
        id = "purify",
        title = "Remove Unwanted Elements",
        shortDescription = "Cleaning up ads and scripts",
        content = """
            The 'purify' or 'Remove Elements' field removes unwanted content.
            
            List selectors for elements to remove, separated by commas.
            Common things to remove: ads, scripts, social buttons, comments.
        """.trimIndent(),
        examples = listOf(
            HelpExample(
                "Remove ads and scripts",
                "div.ads, script, div.advertisement",
                "Removes all matching elements"
            ),
            HelpExample(
                "Remove by class pattern",
                "[class*=ad], [class*=banner]",
                "Removes elements with 'ad' or 'banner' in class"
            ),
            HelpExample(
                "Remove specific elements",
                "div.social-share, div.comments, aside",
                "Removes social buttons, comments, sidebars"
            )
        ),
        tips = listOf(
            "Start with: script, style, div.ads, div.advertisement",
            "Add more selectors if you see unwanted content",
            "Be careful not to remove the actual content!"
        )
    )
    
    // ==================== All Topics ====================
    
    val allTopics = listOf(
        whatIsSelector,
        commonSelectors,
        attributeExtraction,
        fallbackRules,
        searchUrlHelp,
        bookListHelp,
        chapterListHelp,
        contentHelp,
        purifyHelp
    )
    
    fun getTopicById(id: String): HelpTopic? = allTopics.find { it.id == id }
    
    // ==================== Interactive Tutorial ====================
    
    val beginnerTutorial = listOf(
        TutorialStep(
            id = "welcome",
            title = "Welcome to Source Creation!",
            instruction = "This tutorial will guide you through creating your first source. A source tells the app how to read books from a website."
        ),
        TutorialStep(
            id = "basic_info",
            title = "Step 1: Basic Information",
            instruction = "First, enter the website's name and URL. The URL should be the main page (e.g., https://example.com)",
            targetField = "sourceUrl",
            exampleValue = "https://example.com"
        ),
        TutorialStep(
            id = "search_url",
            title = "Step 2: Search URL",
            instruction = "Go to the website and search for any book. Look at the URL in your browser. Replace the search term with {{key}}",
            targetField = "searchUrl",
            exampleValue = "{{baseUrl}}/search?q={{key}}"
        ),
        TutorialStep(
            id = "book_list",
            title = "Step 3: Find Book Items",
            instruction = "On the search results page, right-click a book and select 'Inspect'. Find the container that holds each book.",
            targetField = "ruleSearch.bookList",
            exampleValue = "div.search-result"
        ),
        TutorialStep(
            id = "book_title",
            title = "Step 4: Book Title",
            instruction = "Inside each book container, find the title. It's usually in an <a> or <h3> tag.",
            targetField = "ruleSearch.name",
            exampleValue = "h3 a"
        ),
        TutorialStep(
            id = "book_url",
            title = "Step 5: Book Link",
            instruction = "Find the link to the book's page. Add @href to get the URL.",
            targetField = "ruleSearch.bookUrl",
            exampleValue = "h3 a@href"
        ),
        TutorialStep(
            id = "test_search",
            title = "Step 6: Test Search",
            instruction = "Click 'Test Search' to see if your selectors work. You should see book titles appear."
        ),
        TutorialStep(
            id = "chapters",
            title = "Step 7: Chapter List",
            instruction = "On a book's page, find the chapter list. It's usually a <ul> or series of links.",
            targetField = "ruleToc.chapterList",
            exampleValue = "ul.chapter-list li"
        ),
        TutorialStep(
            id = "content",
            title = "Step 8: Chapter Content",
            instruction = "On a chapter page, find the main text container. It's usually a large <div>.",
            targetField = "ruleContent.content",
            exampleValue = "div.chapter-content"
        ),
        TutorialStep(
            id = "complete",
            title = "Congratulations!",
            instruction = "You've created your first source! Test it by searching for a book and reading a chapter."
        )
    )
    
    // ==================== Quick Tips ====================
    
    val quickTips = listOf(
        "💡 Use browser's 'Inspect Element' (right-click) to see HTML structure",
        "💡 Copy selector from browser: Right-click element → Copy → Copy selector",
        "💡 Test each rule individually before moving to the next",
        "💡 If something doesn't work, try a simpler selector first",
        "💡 Use templates as a starting point for common website types",
        "💡 The Auto-Detect feature can suggest selectors automatically",
        "💡 Import existing Legado sources to learn from their rules",
        "💡 Join the community to share and discover sources"
    )
    
    // ==================== Error Messages ====================
    
    object ErrorMessages {
        fun noResults(selector: String) = "No elements found with selector '$selector'. Try a different selector or check if the page loaded correctly."
        fun invalidSelector(selector: String) = "Invalid selector '$selector'. Check for typos or missing characters."
        fun networkError(url: String) = "Could not load '$url'. Check your internet connection and the URL."
        fun parseError(field: String) = "Could not parse $field. The website structure might have changed."
        
        val commonFixes = mapOf(
            "No books found" to listOf(
                "Check if the search URL is correct",
                "Try searching on the website manually",
                "The book list selector might be wrong"
            ),
            "No chapters found" to listOf(
                "The chapter list might be loaded dynamically (JavaScript)",
                "Try a different chapter list selector",
                "Check if chapters are on a separate page"
            ),
            "Empty content" to listOf(
                "The content might be loaded dynamically",
                "Try a broader content selector",
                "Check if the site requires login"
            )
        )
    }
}
