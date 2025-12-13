

package ireader.core.source.model

typealias FilterList = List<Filter<*>>
typealias CommandList = List<Command<*>>

/**
 * Extension functions for FilterList
 */

/**
 * Get all filters that are not at default value
 */
fun FilterList.getActiveFilters(): FilterList {
    return this.filter { !it.isDefaultValue() }
}

/**
 * Reset all filters to default values
 */
fun FilterList.resetAllFilters() {
    this.forEach { it.reset() }
}

/**
 * Check if any filter is active
 */
fun FilterList.hasActiveFilters(): Boolean {
    return this.any { !it.isDefaultValue() }
}

/**
 * Get filter by name
 */
fun FilterList.findByName(name: String): Filter<*>? {
    return this.firstOrNull { it.name == name }
}

/**
 * Extension functions for CommandList
 */

/**
 * Get all commands that are not at default value
 */
fun CommandList.getActiveCommands(): CommandList {
    return this.filter { !it.isDefaultValue() }
}

/**
 * Reset all commands to default values
 */
fun CommandList.resetAllCommands() {
    this.forEach { it.reset() }
}

/**
 * Check if any command is active
 */
fun CommandList.hasActiveCommands(): Boolean {
    return this.any { !it.isDefaultValue() }
}

/**
 * Find command by name
 */
fun CommandList.findByName(name: String): Command<*>? {
    return this.firstOrNull { it.name == name }
}

/**
 * Find command by type
 */
inline fun <reified T : Command<*>> CommandList.findInstance(): T? {
    return this.filterIsInstance<T>().firstOrNull()
}

/**
 * Find all commands of a specific type
 */
inline fun <reified T : Command<*>> CommandList.findAllInstances(): List<T> {
    return this.filterIsInstance<T>()
}

/**
 * Get all Chapter commands
 */
fun CommandList.getChapterCommands(): List<Command<*>> {
    return this.filter { command ->
        command is Command.Chapter.Note ||
        command is Command.Chapter.Text ||
        command is Command.Chapter.Select ||
        command is Command.Chapter.Fetch ||
        command is Command.Chapter.DateRange ||
        command is Command.Chapter.ChapterType ||
        command is Command.Chapter.ReverseOrder ||
        command is Command.Chapter.GroupBy ||
        command is Command.Chapter.UnreadOnly ||
        command is Command.Chapter.DownloadedOnly ||
        command is Command.Chapter.NumberRange ||
        command is Command.Chapter.ScanlatorFilter
    }
}

/**
 * Get all Content commands
 */
fun CommandList.getContentCommands(): List<Command<*>> {
    return this.filter { command ->
        command is Command.Content.Fetch ||
        command is Command.Content.ImageQuality ||
        command is Command.Content.TextFormat ||
        command is Command.Content.LanguageVariant ||
        command is Command.Content.ServerSelect ||
        command is Command.Content.LazyLoading ||
        command is Command.Content.PreloadPages
    }
}

/**
 * Get all AI commands
 */
fun CommandList.getAICommands(): List<Command<*>> {
    return this.filter { command ->
        command is Command.AI.ChapterSummary ||
        command is Command.AI.TrackCharacters ||
        command is Command.AI.ExtractVocabulary ||
        command is Command.AI.GenerateArtPrompt ||
        command is Command.AI.HighlightPlotPoints ||
        command is Command.AI.MoodIndicator ||
        command is Command.AI.ReadingTimeEstimate ||
        command is Command.AI.ContentWarnings ||
        command is Command.AI.SmartRecap ||
        command is Command.AI.PronunciationGuide ||
        command is Command.AI.TranslationQuality
    }
}

/**
 * Get all Transform commands
 */
fun CommandList.getTransformCommands(): List<Command<*>> {
    return this.filter { command ->
        command is Command.Transform.TTSOptimize ||
        command is Command.Transform.ContentFilter ||
        command is Command.Transform.InlineTranslate ||
        command is Command.Transform.ReadingMode ||
        command is Command.Transform.FontSizeAdjust ||
        command is Command.Transform.LineSpacing ||
        command is Command.Transform.TextAlignment ||
        command is Command.Transform.ChineseConvert ||
        command is Command.Transform.RubyText ||
        command is Command.Transform.MergeParagraphs ||
        command is Command.Transform.ExtractImageText
    }
}

/**
 * Get all Auth commands
 */
fun CommandList.getAuthCommands(): List<Command<*>> {
    return this.filter { command ->
        command is Command.Auth.Username ||
        command is Command.Auth.Password ||
        command is Command.Auth.Token ||
        command is Command.Auth.SessionCookie ||
        command is Command.Auth.PremiumTier ||
        command is Command.Auth.AgeVerification ||
        command is Command.Auth.RememberLogin ||
        command is Command.Auth.TwoFactorCode ||
        command is Command.Auth.OAuthProvider
    }
}

/**
 * Get all Cache commands
 */
fun CommandList.getCacheCommands(): List<Command<*>> {
    return this.filter { command ->
        command is Command.Cache.PrefetchDepth ||
        command is Command.Cache.CachePriority ||
        command is Command.Cache.CacheImages ||
        command is Command.Cache.CacheSizeLimit ||
        command is Command.Cache.AutoClearCache ||
        command is Command.Cache.PrefetchWifiOnly ||
        command is Command.Cache.BackgroundPrefetch
    }
}

/**
 * Get all Social commands
 */
fun CommandList.getSocialCommands(): List<Command<*>> {
    return this.filter { command ->
        command is Command.Social.ShareProgress ||
        command is Command.Social.ShowAnnotations ||
        command is Command.Social.ReadingSession ||
        command is Command.Social.CreateSession ||
        command is Command.Social.PublicStats ||
        command is Command.Social.FriendRecommendations ||
        command is Command.Social.DiscussionLink ||
        command is Command.Social.ReportIssue ||
        command is Command.Social.ChapterRating
    }
}

/**
 * Get all Migration commands
 */
fun CommandList.getMigrationCommands(): List<Command<*>> {
    return this.filter { command ->
        command is Command.Migration.ChapterMatcher ||
        command is Command.Migration.PreserveProgress ||
        command is Command.Migration.PreserveBookmarks ||
        command is Command.Migration.PreserveDownloads ||
        command is Command.Migration.ShowPreview ||
        command is Command.Migration.BackupFirst ||
        command is Command.Migration.SourcePriority ||
        command is Command.Migration.MissingChapterAction
    }
}

/**
 * Get all Batch commands
 */
fun CommandList.getBatchCommands(): List<Command<*>> {
    return this.filter { command ->
        command is Command.Batch.DownloadRange ||
        command is Command.Batch.RangeStart ||
        command is Command.Batch.RangeEnd ||
        command is Command.Batch.MarkAsRead ||
        command is Command.Batch.DeleteDownloaded ||
        command is Command.Batch.ExportFormat ||
        command is Command.Batch.ConfirmBatch
    }
}

/**
 * Get all Explore commands
 */
fun CommandList.getExploreCommands(): List<Command<*>> {
    return this.filter { command ->
        command is Command.Explore.Fetch ||
        command is Command.Explore.FindSimilar ||
        command is Command.Explore.Recommendations ||
        command is Command.Explore.CrossSourceSearch ||
        command is Command.Explore.IncludeAdult ||
        command is Command.Explore.MinimumRating ||
        command is Command.Explore.CompletionStatus ||
        command is Command.Explore.SortOrder
    }
}

/**
 * Group commands by category for UI display
 */
fun CommandList.groupByCategory(): Map<String, List<Command<*>>> {
    val result = mutableMapOf<String, MutableList<Command<*>>>()
    
    this.forEach { command ->
        val category = when (command) {
            is Command.Chapter.Note, is Command.Chapter.Text, is Command.Chapter.Select,
            is Command.Chapter.Fetch, is Command.Chapter.DateRange, is Command.Chapter.ChapterType,
            is Command.Chapter.ReverseOrder, is Command.Chapter.GroupBy, is Command.Chapter.UnreadOnly,
            is Command.Chapter.DownloadedOnly, is Command.Chapter.NumberRange, is Command.Chapter.ScanlatorFilter -> "Chapter"
            
            is Command.Content.Fetch, is Command.Content.ImageQuality, is Command.Content.TextFormat,
            is Command.Content.LanguageVariant, is Command.Content.ServerSelect, is Command.Content.LazyLoading,
            is Command.Content.PreloadPages -> "Content"
            
            is Command.Detail.Fetch, is Command.Detail.IncludeMetadata, is Command.Detail.FetchRelated,
            is Command.Detail.CoverSource -> "Detail"
            
            is Command.Explore.Fetch, is Command.Explore.FindSimilar, is Command.Explore.Recommendations,
            is Command.Explore.CrossSourceSearch, is Command.Explore.IncludeAdult, is Command.Explore.MinimumRating,
            is Command.Explore.CompletionStatus, is Command.Explore.SortOrder -> "Explore"
            
            is Command.AI.ChapterSummary, is Command.AI.TrackCharacters, is Command.AI.ExtractVocabulary,
            is Command.AI.GenerateArtPrompt, is Command.AI.HighlightPlotPoints, is Command.AI.MoodIndicator,
            is Command.AI.ReadingTimeEstimate, is Command.AI.ContentWarnings, is Command.AI.SmartRecap,
            is Command.AI.PronunciationGuide, is Command.AI.TranslationQuality -> "AI"
            
            is Command.Auth.Username, is Command.Auth.Password, is Command.Auth.Token,
            is Command.Auth.SessionCookie, is Command.Auth.PremiumTier, is Command.Auth.AgeVerification,
            is Command.Auth.RememberLogin, is Command.Auth.TwoFactorCode, is Command.Auth.OAuthProvider -> "Auth"
            
            is Command.Batch.DownloadRange, is Command.Batch.RangeStart, is Command.Batch.RangeEnd,
            is Command.Batch.MarkAsRead, is Command.Batch.DeleteDownloaded, is Command.Batch.ExportFormat,
            is Command.Batch.ConfirmBatch -> "Batch"
            
            is Command.Transform.TTSOptimize, is Command.Transform.ContentFilter, is Command.Transform.InlineTranslate,
            is Command.Transform.ReadingMode, is Command.Transform.FontSizeAdjust, is Command.Transform.LineSpacing,
            is Command.Transform.TextAlignment, is Command.Transform.ChineseConvert, is Command.Transform.RubyText,
            is Command.Transform.MergeParagraphs, is Command.Transform.ExtractImageText -> "Transform"
            
            is Command.Cache.PrefetchDepth, is Command.Cache.CachePriority, is Command.Cache.CacheImages,
            is Command.Cache.CacheSizeLimit, is Command.Cache.AutoClearCache, is Command.Cache.PrefetchWifiOnly,
            is Command.Cache.BackgroundPrefetch -> "Cache"
            
            is Command.Social.ShareProgress, is Command.Social.ShowAnnotations, is Command.Social.ReadingSession,
            is Command.Social.CreateSession, is Command.Social.PublicStats, is Command.Social.FriendRecommendations,
            is Command.Social.DiscussionLink, is Command.Social.ReportIssue, is Command.Social.ChapterRating -> "Social"
            
            is Command.Migration.ChapterMatcher, is Command.Migration.PreserveProgress, is Command.Migration.PreserveBookmarks,
            is Command.Migration.PreserveDownloads, is Command.Migration.ShowPreview, is Command.Migration.BackupFirst,
            is Command.Migration.SourcePriority, is Command.Migration.MissingChapterAction -> "Migration"
            
            is Command.Fetchers -> "Fetchers"
            is Command.Note -> "Notes"
            is Command.Text -> "Text"
            is Command.Select -> "Select"
            is Command.Toggle -> "Toggle"
            is Command.Range -> "Range"
            
            else -> "Other"
        }
        
        result.getOrPut(category) { mutableListOf() }.add(command)
    }
    
    return result
}
