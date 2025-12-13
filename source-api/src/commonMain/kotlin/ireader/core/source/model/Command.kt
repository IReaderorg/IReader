package ireader.core.source.model

/**
 * Command system for source operations.
 * 
 * Commands allow sources to expose configurable options that users can modify
 * to customize how content is fetched, filtered, transformed, and displayed.
 * 
 * Categories:
 * - Fetchers: URL/HTML injection for custom fetching
 * - Chapter: Chapter listing customization
 * - Content: Content quality and format options
 * - Detail: Book detail customization
 * - Explore: Discovery and search enhancements
 * - AI: AI-powered reading enhancements
 * - Auth: Authentication and premium access
 * - Batch: Bulk operations
 * - Transform: Content transformation
 * - Cache: Caching and prefetch control
 * - Social: Community features
 * - Migration: Source migration helpers
 */
sealed class Command<V>(val name: String, val initialValue: V) {

    /**
     * The value of this command, with the initial value set.
     */
    var value = initialValue

    /**
     * Whether this command has been updated. If this method returns true, the catalog won't receive
     * this command when performing an operation.
     */
    open fun isDefaultValue(): Boolean {
        return value == initialValue
    }
    
    /**
     * Reset command to initial value
     */
    fun reset() {
        value = initialValue
    }
    
    /**
     * Get a human-readable description of the current value
     */
    open fun getValueDescription(): String = value.toString()

    // ==================== Base Command Types ====================
    
    /**
     * Base class for URL/HTML fetcher commands
     */
    open class Fetchers(open val url: String = "", open val html: String = "") : Command<String>(url, html) {
        fun hasUrl(): Boolean = url.isNotBlank()
        fun hasHtml(): Boolean = html.isNotBlank()
        fun isValid(): Boolean = hasUrl() || hasHtml()
    }
    
    /**
     * Simple note/label command (no user input)
     */
    open class Note(name: String) : Command<Unit>(name, Unit)
    
    /**
     * Text input command
     */
    open class Text(name: String, value: String = "", val hint: String = "") : Command<String>(name, value)
    
    /**
     * Selection command with predefined options
     */
    open class Select(
        name: String,
        open val options: Array<String>,
        value: Int = 0,
        open val description: String = ""
    ) : Command<Int>(name, value) {
        override fun getValueDescription(): String = options.getOrNull(value) ?: "Unknown"
    }
    
    /**
     * Boolean toggle command
     */
    open class Toggle(name: String, value: Boolean = false, open val description: String = "") : Command<Boolean>(name, value)
    
    /**
     * Number range command
     */
    open class Range(
        name: String,
        value: Int = 0,
        val min: Int = 0,
        val max: Int = 100,
        val step: Int = 1
    ) : Command<Int>(name, value)

    // ==================== Detail Commands ====================
    
    /**
     * Commands for customizing book detail fetching
     */
    object Detail {
        open class Fetch(override val url: String = "", override val html: String = "") : Fetchers(url, html)
        
        /** Include additional metadata like ratings, reviews count */
        open class IncludeMetadata(name: String = "Include Metadata") : Toggle(name, true)
        
        /** Fetch related/similar books */
        open class FetchRelated(name: String = "Fetch Related Books") : Toggle(name, false)
        
        /** Alternative cover source */
        open class CoverSource(
            name: String = "Cover Source",
            options: Array<String> = arrayOf("Default", "High Resolution", "Alternative", "Fan Art")
        ) : Select(name, options, 0)
    }

    // ==================== Content Commands ====================
    
    /**
     * Commands for customizing chapter content fetching and quality
     */
    object Content {
        open class Fetch(override val url: String = "", override val html: String = "") : Fetchers(url, html)
        
        /** Image quality for manga/comic sources */
        open class ImageQuality(
            name: String = "Image Quality",
            options: Array<String> = arrayOf("Auto", "Low (Data Saver)", "Medium", "High", "Original")
        ) : Select(name, options, 0, "Select image quality for manga pages")
        
        /** Text formatting preference */
        open class TextFormat(
            name: String = "Text Format",
            options: Array<String> = arrayOf("Default", "Clean (No Ads)", "With Translator Notes", "Raw")
        ) : Select(name, options, 0, "Choose how text content is formatted")
        
        /** Language variant for multilingual sources */
        open class LanguageVariant(
            name: String = "Translation",
            options: Array<String> = arrayOf("Official", "Fan Translation", "MTL", "Original")
        ) : Select(name, options, 0, "Select translation type")
        
        /** Server selection for sources with multiple servers */
        open class ServerSelect(
            name: String = "Server",
            options: Array<String> = arrayOf("Auto", "Server 1", "Server 2", "Server 3")
        ) : Select(name, options, 0, "Select content server")
        
        /** Load images lazily or all at once */
        open class LazyLoading(name: String = "Lazy Load Images") : Toggle(name, true, "Load images as you scroll")
        
        /** Preload next pages */
        open class PreloadPages(
            name: String = "Preload Pages",
            options: Array<String> = arrayOf("Off", "Next 2", "Next 5", "Next 10")
        ) : Select(name, options, 1)
    }

    // ==================== Explore Commands ====================
    
    /**
     * Commands for customizing discovery and search
     */
    object Explore {
        open class Fetch(override val url: String = "", override val html: String = "") : Fetchers(url, html)
        
        /** Find similar novels based on a book key */
        open class FindSimilar(
            name: String = "Find Similar",
            val bookKey: String = "",
            val bookTitle: String = ""
        ) : Text(name, bookKey, "Enter book key or title")
        
        /** Recommendation engine type */
        open class Recommendations(
            name: String = "Recommendations",
            options: Array<String> = arrayOf("Popular", "Based on Library", "Hidden Gems", "New Releases", "Trending", "Editor's Pick")
        ) : Select(name, options, 0, "Choose recommendation algorithm")
        
        /** Cross-source search toggle */
        open class CrossSourceSearch(name: String = "Search All Sources") : Toggle(name, false, "Search across all installed sources")
        
        /** Include adult content */
        open class IncludeAdult(name: String = "Include Adult Content") : Toggle(name, false)
        
        /** Minimum rating filter */
        open class MinimumRating(
            name: String = "Minimum Rating",
            options: Array<String> = arrayOf("Any", "3+ Stars", "4+ Stars", "4.5+ Stars")
        ) : Select(name, options, 0)
        
        /** Completion status filter */
        open class CompletionStatus(
            name: String = "Status",
            options: Array<String> = arrayOf("Any", "Ongoing", "Completed", "Hiatus")
        ) : Select(name, options, 0)
        
        /** Sort order */
        open class SortOrder(
            name: String = "Sort By",
            options: Array<String> = arrayOf("Relevance", "Latest Update", "Most Popular", "Rating", "Chapter Count", "Title A-Z")
        ) : Select(name, options, 0)
    }

    // ==================== Chapter Commands ====================
    
    /**
     * Commands for customizing chapter list fetching
     */
    object Chapter {
        class Note(name: String) : Command.Note(name)
        open class Text(name: String, value: String = "") : Command.Text(name, value)
        open class Select(
            name: String,
            override val options: Array<String>,
            value: Int = 0
        ) : Command.Select(name, options, value)
        open class Fetch(override val url: String = "", override val html: String = "") : Fetchers(url, html)
        
        /** Filter chapters by date range */
        open class DateRange(
            name: String = "Date Range",
            options: Array<String> = arrayOf("All", "Last Week", "Last Month", "Last 3 Months", "Last Year", "Custom")
        ) : Command.Select(name, options, 0, "Filter chapters by release date")
        
        /** Filter by chapter type */
        open class ChapterType(
            name: String = "Chapter Type",
            options: Array<String> = arrayOf("All", "Free Only", "Premium", "Bonus/Side Stories", "Main Story Only")
        ) : Command.Select(name, options, 0, "Filter by chapter type")
        
        /** Reverse chapter order */
        open class ReverseOrder(name: String = "Reverse Order") : Toggle(name, false, "Show newest chapters first")
        
        /** Group chapters by volume/arc */
        open class GroupBy(
            name: String = "Group By",
            options: Array<String> = arrayOf("None", "Volume", "Arc", "Season")
        ) : Command.Select(name, options, 0)
        
        /** Show only unread chapters */
        open class UnreadOnly(name: String = "Unread Only") : Toggle(name, false, "Show only unread chapters")
        
        /** Show only downloaded chapters */
        open class DownloadedOnly(name: String = "Downloaded Only") : Toggle(name, false, "Show only downloaded chapters")
        
        /** Chapter number range filter */
        open class NumberRange(
            name: String = "Chapter Range",
            val startChapter: Float = 0f,
            val endChapter: Float = Float.MAX_VALUE
        ) : Command<Pair<Float, Float>>(name, Pair(startChapter, endChapter))
        
        /** Scanlator/translator group filter */
        open class ScanlatorFilter(
            name: String = "Scanlator",
            val scanlators: Array<String> = arrayOf("All")
        ) : Command.Select(name, scanlators, 0, "Filter by translation group")
    }

    // ==================== AI Commands ====================
    
    /**
     * AI-powered reading enhancement commands
     */
    object AI {
        /** Generate chapter summary before reading */
        open class ChapterSummary(name: String = "Generate Summary") : Toggle(name, false, "AI-generate a brief summary of the chapter")
        
        /** Track and identify characters */
        open class TrackCharacters(name: String = "Track Characters") : Toggle(name, false, "AI tracks character appearances and relationships")
        
        /** Extract vocabulary for language learners */
        open class ExtractVocabulary(
            name: String = "Extract Vocabulary",
            options: Array<String> = arrayOf("Off", "HSK 1-3", "HSK 4-6", "JLPT N5-N3", "JLPT N2-N1", "All New Words")
        ) : Select(name, options, 0, "Extract vocabulary words for language learning")
        
        /** Generate character art prompt from descriptions */
        open class GenerateArtPrompt(name: String = "Generate Art Prompt") : Toggle(name, false, "Generate AI art prompts from character descriptions")
        
        /** Auto-detect and highlight important plot points */
        open class HighlightPlotPoints(name: String = "Highlight Plot Points") : Toggle(name, false, "AI highlights important story moments")
        
        /** Sentiment analysis for mood indication */
        open class MoodIndicator(name: String = "Show Mood Indicator") : Toggle(name, false, "Display chapter mood/tone indicator")
        
        /** Reading time estimation */
        open class ReadingTimeEstimate(name: String = "Show Reading Time") : Toggle(name, true, "Display estimated reading time")
        
        /** Content warnings detection */
        open class ContentWarnings(name: String = "Content Warnings") : Toggle(name, true, "AI-detect and show content warnings")
        
        /** Smart recap of previous chapters */
        open class SmartRecap(name: String = "Smart Recap") : Toggle(name, false, "Show AI-generated recap of previous chapters")
        
        /** Pronunciation guide for names */
        open class PronunciationGuide(name: String = "Pronunciation Guide") : Toggle(name, false, "Show pronunciation for character/place names")
        
        /** Translation quality indicator */
        open class TranslationQuality(
            name: String = "Translation Quality",
            options: Array<String> = arrayOf("Off", "Basic Check", "Detailed Analysis")
        ) : Select(name, options, 0, "Analyze and rate translation quality")
    }

    // ==================== Auth Commands ====================
    
    /**
     * Authentication and premium access commands
     */
    object Auth {
        /** Username/email for login */
        open class Username(name: String = "Username") : Text(name, "", "Enter username or email")
        
        /** Password (should be handled securely) */
        open class Password(name: String = "Password") : Text(name, "", "Enter password")
        
        /** API token for authenticated requests */
        open class Token(name: String = "API Token") : Text(name, "", "Enter API token")
        
        /** Session cookie for sites requiring login */
        open class SessionCookie(name: String = "Session Cookie") : Text(name, "", "Paste session cookie")
        
        /** Premium tier selection */
        open class PremiumTier(
            name: String = "Access Level",
            options: Array<String> = arrayOf("Free", "Basic", "Premium", "VIP", "Patreon")
        ) : Select(name, options, 0, "Select your subscription tier")
        
        /** Age verification */
        open class AgeVerification(name: String = "Age Verified (18+)") : Toggle(name, false, "Confirm you are 18 or older")
        
        /** Remember login */
        open class RememberLogin(name: String = "Remember Login") : Toggle(name, true, "Stay logged in")
        
        /** Two-factor auth code */
        open class TwoFactorCode(name: String = "2FA Code") : Text(name, "", "Enter 2FA code")
        
        /** OAuth provider selection */
        open class OAuthProvider(
            name: String = "Login With",
            options: Array<String> = arrayOf("Email", "Google", "Discord", "Twitter")
        ) : Select(name, options, 0)
    }

    // ==================== Batch Commands ====================
    
    /**
     * Bulk operation commands
     */
    object Batch {
        /** Download chapter range */
        open class DownloadRange(
            name: String = "Download Range",
            options: Array<String> = arrayOf("All", "Unread Only", "First 10", "First 50", "Last 10", "Custom Range")
        ) : Select(name, options, 0, "Select chapters to download")
        
        /** Custom range start */
        open class RangeStart(name: String = "Start Chapter") : Text(name, "1", "First chapter number")
        
        /** Custom range end */
        open class RangeEnd(name: String = "End Chapter") : Text(name, "", "Last chapter number (empty = all)")
        
        /** Mark chapters as read */
        open class MarkAsRead(
            name: String = "Mark as Read",
            options: Array<String> = arrayOf("None", "First 10", "First 50", "All Before Current", "All")
        ) : Select(name, options, 0, "Bulk mark chapters as read")
        
        /** Delete downloaded chapters */
        open class DeleteDownloaded(
            name: String = "Delete Downloads",
            options: Array<String> = arrayOf("None", "Read Chapters", "All Except Last 10", "All")
        ) : Select(name, options, 0, "Bulk delete downloaded chapters")
        
        /** Export chapters */
        open class ExportFormat(
            name: String = "Export Format",
            options: Array<String> = arrayOf("EPUB", "PDF", "TXT", "HTML", "CBZ")
        ) : Select(name, options, 0, "Format for chapter export")
        
        /** Batch operation confirmation */
        open class ConfirmBatch(name: String = "Confirm Batch Operation") : Toggle(name, false, "Require confirmation for batch operations")
    }

    // ==================== Transform Commands ====================
    
    /**
     * Content transformation commands
     */
    object Transform {
        /** Optimize text for TTS */
        open class TTSOptimize(name: String = "Optimize for TTS") : Toggle(name, false, "Clean text for better text-to-speech")
        
        /** Content filter level */
        open class ContentFilter(
            name: String = "Content Filter",
            options: Array<String> = arrayOf("None", "Remove Ads", "Remove Author Notes", "Remove Watermarks", "Clean All")
        ) : Select(name, options, 0, "Filter unwanted content")
        
        /** Inline translation target language */
        open class InlineTranslate(
            name: String = "Inline Translation",
            options: Array<String> = arrayOf("Off", "English", "Spanish", "French", "German", "Japanese", "Korean", "Chinese")
        ) : Select(name, options, 0, "Translate content inline")
        
        /** Reading mode transformation */
        open class ReadingMode(
            name: String = "Reading Mode",
            options: Array<String> = arrayOf("Normal", "Night Mode Text", "Dyslexia Friendly", "Speed Reading", "Focus Mode")
        ) : Select(name, options, 0, "Transform text for different reading modes")
        
        /** Font size adjustment */
        open class FontSizeAdjust(
            name: String = "Font Size",
            options: Array<String> = arrayOf("Default", "Small", "Medium", "Large", "Extra Large")
        ) : Select(name, options, 0)
        
        /** Line spacing adjustment */
        open class LineSpacing(
            name: String = "Line Spacing",
            options: Array<String> = arrayOf("Compact", "Normal", "Relaxed", "Double")
        ) : Select(name, options, 1)
        
        /** Text alignment */
        open class TextAlignment(
            name: String = "Text Alignment",
            options: Array<String> = arrayOf("Left", "Justified", "Center")
        ) : Select(name, options, 0)
        
        /** Convert traditional/simplified Chinese */
        open class ChineseConvert(
            name: String = "Chinese Conversion",
            options: Array<String> = arrayOf("None", "To Simplified", "To Traditional")
        ) : Select(name, options, 0)
        
        /** Ruby text (furigana) display */
        open class RubyText(
            name: String = "Ruby Text (Furigana)",
            options: Array<String> = arrayOf("Show", "Hide", "On Hover Only")
        ) : Select(name, options, 0)
        
        /** Paragraph merging for better flow */
        open class MergeParagraphs(name: String = "Merge Short Paragraphs") : Toggle(name, false, "Combine very short paragraphs")
        
        /** Image text extraction (OCR) */
        open class ExtractImageText(name: String = "Extract Text from Images") : Toggle(name, false, "OCR text from images")
    }

    // ==================== Cache Commands ====================
    
    /**
     * Caching and prefetch control commands
     */
    object Cache {
        /** Prefetch depth for offline reading */
        open class PrefetchDepth(
            name: String = "Prefetch Chapters",
            options: Array<String> = arrayOf("Off", "Next 5", "Next 10", "Next 25", "All Unread")
        ) : Select(name, options, 1, "Prefetch chapters for offline reading")
        
        /** Cache priority/retention */
        open class CachePriority(
            name: String = "Cache Priority",
            options: Array<String> = arrayOf("Normal", "High (Keep Longer)", "Temporary", "Permanent")
        ) : Select(name, options, 0, "How long to keep cached content")
        
        /** Cache images */
        open class CacheImages(name: String = "Cache Images") : Toggle(name, true, "Cache images for offline viewing")
        
        /** Cache size limit */
        open class CacheSizeLimit(
            name: String = "Cache Size Limit",
            options: Array<String> = arrayOf("100 MB", "250 MB", "500 MB", "1 GB", "Unlimited")
        ) : Select(name, options, 2)
        
        /** Auto-clear old cache */
        open class AutoClearCache(
            name: String = "Auto Clear Cache",
            options: Array<String> = arrayOf("Never", "After 1 Week", "After 1 Month", "After 3 Months")
        ) : Select(name, options, 2)
        
        /** Prefetch on WiFi only */
        open class PrefetchWifiOnly(name: String = "Prefetch on WiFi Only") : Toggle(name, true, "Only prefetch when connected to WiFi")
        
        /** Background prefetch */
        open class BackgroundPrefetch(name: String = "Background Prefetch") : Toggle(name, false, "Prefetch chapters in background")
    }

    // ==================== Social Commands ====================
    
    /**
     * Community and social feature commands
     */
    object Social {
        /** Share reading progress */
        open class ShareProgress(name: String = "Share Progress") : Toggle(name, false, "Share your reading progress")
        
        /** Show community annotations/comments */
        open class ShowAnnotations(
            name: String = "Community Notes",
            options: Array<String> = arrayOf("Off", "Spoiler-Free", "All", "Top Rated Only", "Friends Only")
        ) : Select(name, options, 0, "Show community annotations")
        
        /** Join collaborative reading session */
        open class ReadingSession(name: String = "Reading Session ID") : Text(name, "", "Enter session ID to join")
        
        /** Create reading session */
        open class CreateSession(name: String = "Create Reading Session") : Toggle(name, false, "Start a collaborative reading session")
        
        /** Show reading statistics publicly */
        open class PublicStats(name: String = "Public Statistics") : Toggle(name, false, "Show your reading stats on profile")
        
        /** Allow friend recommendations */
        open class FriendRecommendations(name: String = "Friend Recommendations") : Toggle(name, true, "Receive book recommendations from friends")
        
        /** Discussion thread link */
        open class DiscussionLink(name: String = "Discussion Thread") : Text(name, "", "Link to discussion thread")
        
        /** Report content issues */
        open class ReportIssue(
            name: String = "Report Issue",
            options: Array<String> = arrayOf("None", "Missing Content", "Wrong Chapter", "Bad Translation", "Broken Images", "Other")
        ) : Select(name, options, 0)
        
        /** Upvote/rate chapter */
        open class ChapterRating(
            name: String = "Rate Chapter",
            options: Array<String> = arrayOf("Not Rated", "⭐", "⭐⭐", "⭐⭐⭐", "⭐⭐⭐⭐", "⭐⭐⭐⭐⭐")
        ) : Select(name, options, 0)
    }

    // ==================== Migration Commands ====================
    
    /**
     * Source migration helper commands
     */
    object Migration {
        /** Chapter matching strategy */
        open class ChapterMatcher(
            name: String = "Chapter Matching",
            options: Array<String> = arrayOf("By Title", "By Number", "By Content Hash", "Manual", "Fuzzy Match")
        ) : Select(name, options, 0, "How to match chapters between sources")
        
        /** Preserve reading position */
        open class PreserveProgress(name: String = "Keep Reading Position") : Toggle(name, true, "Preserve your reading progress")
        
        /** Preserve bookmarks */
        open class PreserveBookmarks(name: String = "Keep Bookmarks") : Toggle(name, true, "Preserve chapter bookmarks")
        
        /** Preserve downloaded chapters */
        open class PreserveDownloads(name: String = "Keep Downloads") : Toggle(name, false, "Re-download chapters from new source")
        
        /** Migration preview */
        open class ShowPreview(name: String = "Show Migration Preview") : Toggle(name, true, "Preview changes before migrating")
        
        /** Backup before migration */
        open class BackupFirst(name: String = "Backup Before Migration") : Toggle(name, true, "Create backup before migrating")
        
        /** Source priority for auto-migration */
        open class SourcePriority(
            name: String = "Source Priority",
            options: Array<String> = arrayOf("Prefer Official", "Prefer Fan Translation", "Prefer Fastest", "Prefer Most Chapters")
        ) : Select(name, options, 0)
        
        /** Handle missing chapters */
        open class MissingChapterAction(
            name: String = "Missing Chapters",
            options: Array<String> = arrayOf("Skip", "Keep from Old Source", "Mark as Unavailable")
        ) : Select(name, options, 1)
    }
}
