package ireader.domain.plugins

import ireader.domain.utils.extensions.formatDateTime

/**
 * Utility for formatting plugin changelogs
 * Requirements: 12.3
 */
object PluginChangelogFormatter {
    
    /**
     * Format a changelog for display
     * Supports markdown-style formatting
     */
    fun format(changelog: String): String {
        if (changelog.isBlank()) {
            return "No changelog available"
        }
        
        return changelog.trim()
    }
    
    /**
     * Format multiple changelogs for a version history
     */
    fun formatHistory(updates: List<PluginUpdate>): String {
        if (updates.isEmpty()) {
            return "No update history available"
        }
        
        return updates.joinToString("\n\n") { update ->
            """
            Version ${update.latestVersion} (${update.latestVersionCode})
            Released: ${formatDate(update.releaseDate)}
            
            ${format(update.changelog)}
            """.trimIndent()
        }
    }
    
    /**
     * Format a date timestamp
     */
    private fun formatDate(timestamp: Long): String {
        return timestamp.formatDateTime()
    }
    
    /**
     * Extract summary from changelog (first line or first 100 chars)
     */
    fun extractSummary(changelog: String, maxLength: Int = 100): String {
        if (changelog.isBlank()) {
            return "No description"
        }
        
        val firstLine = changelog.lines().firstOrNull { it.isNotBlank() } ?: changelog
        
        return if (firstLine.length > maxLength) {
            firstLine.take(maxLength) + "..."
        } else {
            firstLine
        }
    }
}
