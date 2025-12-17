package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for glossary and dictionary management.
 * Supports translation glossaries, character databases, and term dictionaries.
 * 
 * Example:
 * ```kotlin
 * class TranslationGlossaryPlugin : GlossaryPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.translation-glossary",
 *         name = "Translation Glossary",
 *         type = PluginType.GLOSSARY,
 *         permissions = listOf(PluginPermission.GLOSSARY_ACCESS, PluginPermission.STORAGE),
 *         // ... other manifest fields
 *     )
 *     
 *     override val glossaryType = GlossaryType.TRANSLATION
 *     
 *     override suspend fun lookupTerm(term: String): GlossaryResult<List<GlossaryEntry>> {
 *         // Look up term in glossary
 *     }
 * }
 * ```
 */
interface GlossaryPlugin : Plugin {
    /**
     * Type of glossary.
     */
    val glossaryType: GlossaryType
    
    /**
     * Glossary configuration.
     */
    val glossaryConfig: GlossaryConfig
    
    /**
     * Get all glossaries managed by this plugin.
     */
    suspend fun getGlossaries(): GlossaryResult<List<Glossary>>
    
    /**
     * Create a new glossary.
     */
    suspend fun createGlossary(glossary: Glossary): GlossaryResult<Glossary>
    
    /**
     * Delete a glossary.
     */
    suspend fun deleteGlossary(glossaryId: String): GlossaryResult<Unit>
    
    /**
     * Look up a term in glossaries.
     */
    suspend fun lookupTerm(
        term: String,
        glossaryIds: List<String> = emptyList(),
        options: LookupOptions = LookupOptions()
    ): GlossaryResult<List<GlossaryEntry>>
    
    /**
     * Add entry to glossary.
     */
    suspend fun addEntry(glossaryId: String, entry: GlossaryEntry): GlossaryResult<GlossaryEntry>
    
    /**
     * Update entry in glossary.
     */
    suspend fun updateEntry(glossaryId: String, entry: GlossaryEntry): GlossaryResult<GlossaryEntry>
    
    /**
     * Delete entry from glossary.
     */
    suspend fun deleteEntry(glossaryId: String, entryId: String): GlossaryResult<Unit>
    
    /**
     * Search entries in glossary.
     */
    suspend fun searchEntries(
        glossaryId: String,
        query: String,
        options: SearchOptions = SearchOptions()
    ): GlossaryResult<List<GlossaryEntry>>
    
    /**
     * Import glossary from file/URL.
     */
    suspend fun importGlossary(source: GlossaryImportSource): GlossaryResult<Glossary>
    
    /**
     * Export glossary to file.
     */
    suspend fun exportGlossary(glossaryId: String, format: GlossaryFormat): GlossaryResult<ByteArray>
    
    /**
     * Apply glossary to text (replace terms).
     */
    suspend fun applyGlossary(
        text: String,
        glossaryIds: List<String>,
        options: ApplyOptions = ApplyOptions()
    ): GlossaryResult<GlossaryApplyResult>
    
    /**
     * Get statistics for glossary.
     */
    suspend fun getStatistics(glossaryId: String): GlossaryResult<GlossaryStatistics>
}

/**
 * Type of glossary.
 */
@Serializable
enum class GlossaryType {
    /** Translation glossary (term -> translation) */
    TRANSLATION,
    /** Character database */
    CHARACTER,
    /** Dictionary (term -> definition) */
    DICTIONARY,
    /** Name glossary (names, places) */
    NAMES,
    /** Custom glossary */
    CUSTOM
}

/**
 * Glossary configuration.
 */
@Serializable
data class GlossaryConfig(
    /** Maximum entries per glossary */
    val maxEntriesPerGlossary: Int = 100000,
    /** Maximum glossaries */
    val maxGlossaries: Int = 100,
    /** Supported import formats */
    val supportedImportFormats: List<GlossaryFormat> = listOf(GlossaryFormat.JSON, GlossaryFormat.CSV),
    /** Supported export formats */
    val supportedExportFormats: List<GlossaryFormat> = listOf(GlossaryFormat.JSON, GlossaryFormat.CSV),
    /** Whether fuzzy matching is supported */
    val supportsFuzzyMatch: Boolean = true,
    /** Whether regex matching is supported */
    val supportsRegex: Boolean = false,
    /** Whether bulk operations are supported */
    val supportsBulkOperations: Boolean = true
)

/**
 * Glossary definition.
 */
@Serializable
data class Glossary(
    /** Unique glossary identifier */
    val id: String,
    /** Glossary name */
    val name: String,
    /** Glossary description */
    val description: String? = null,
    /** Glossary type */
    val type: GlossaryType,
    /** Source language (for translation glossaries) */
    val sourceLanguage: String? = null,
    /** Target language (for translation glossaries) */
    val targetLanguage: String? = null,
    /** Associated book/series IDs */
    val associatedBookIds: List<String> = emptyList(),
    /** Entry count */
    val entryCount: Int = 0,
    /** Created timestamp */
    val createdAt: Long,
    /** Last updated timestamp */
    val updatedAt: Long,
    /** Whether glossary is enabled */
    val isEnabled: Boolean = true,
    /** Priority (higher = applied first) */
    val priority: Int = 0,
    /** Tags */
    val tags: List<String> = emptyList()
)

/**
 * Glossary entry.
 */
@Serializable
data class GlossaryEntry(
    /** Entry ID */
    val id: String,
    /** Original term */
    val term: String,
    /** Translation/definition */
    val value: String,
    /** Reading/pronunciation (for CJK) */
    val reading: String? = null,
    /** Part of speech */
    val partOfSpeech: String? = null,
    /** Context/usage notes */
    val notes: String? = null,
    /** Example sentences */
    val examples: List<String> = emptyList(),
    /** Related terms */
    val relatedTerms: List<String> = emptyList(),
    /** Aliases (alternative forms) */
    val aliases: List<String> = emptyList(),
    /** Whether entry is case-sensitive */
    val caseSensitive: Boolean = false,
    /** Whether to match whole word only */
    val wholeWordOnly: Boolean = true,
    /** Entry priority */
    val priority: Int = 0,
    /** Created timestamp */
    val createdAt: Long,
    /** Last updated timestamp */
    val updatedAt: Long,
    /** Additional metadata */
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Lookup options.
 */
@Serializable
data class LookupOptions(
    /** Whether to use fuzzy matching */
    val fuzzyMatch: Boolean = false,
    /** Fuzzy match threshold (0-1) */
    val fuzzyThreshold: Float = 0.8f,
    /** Whether to match case-insensitively */
    val caseInsensitive: Boolean = true,
    /** Maximum results */
    val maxResults: Int = 10,
    /** Include aliases in search */
    val includeAliases: Boolean = true
)

/**
 * Search options.
 */
@Serializable
data class SearchOptions(
    /** Search in term */
    val searchInTerm: Boolean = true,
    /** Search in value */
    val searchInValue: Boolean = true,
    /** Search in notes */
    val searchInNotes: Boolean = false,
    /** Case insensitive */
    val caseInsensitive: Boolean = true,
    /** Maximum results */
    val maxResults: Int = 100,
    /** Sort by */
    val sortBy: EntrySortOption = EntrySortOption.RELEVANCE,
    /** Page number */
    val page: Int = 1,
    /** Page size */
    val pageSize: Int = 50
)

@Serializable
enum class EntrySortOption {
    RELEVANCE,
    TERM_ASC,
    TERM_DESC,
    CREATED_ASC,
    CREATED_DESC,
    UPDATED_ASC,
    UPDATED_DESC,
    PRIORITY
}

/**
 * Glossary import source.
 */
@Serializable
data class GlossaryImportSource(
    /** Import type */
    val type: ImportSourceType,
    /** File data (for FILE type) */
    val fileData: ByteArray? = null,
    /** URL (for URL type) */
    val url: String? = null,
    /** Format */
    val format: GlossaryFormat,
    /** Glossary name (for new glossary) */
    val glossaryName: String? = null,
    /** Target glossary ID (for merge) */
    val targetGlossaryId: String? = null,
    /** Import options */
    val options: ImportOptions = ImportOptions()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as GlossaryImportSource
        return type == other.type &&
                fileData?.contentEquals(other.fileData) != false &&
                url == other.url &&
                format == other.format
    }
    
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (fileData?.contentHashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + format.hashCode()
        return result
    }
}

@Serializable
enum class ImportSourceType {
    FILE,
    URL
}

@Serializable
data class ImportOptions(
    /** Whether to merge with existing */
    val mergeWithExisting: Boolean = false,
    /** How to handle duplicates */
    val duplicateHandling: DuplicateHandling = DuplicateHandling.SKIP,
    /** Whether to validate entries */
    val validateEntries: Boolean = true
)

@Serializable
enum class DuplicateHandling {
    SKIP,
    OVERWRITE,
    RENAME
}

/**
 * Glossary format.
 */
@Serializable
enum class GlossaryFormat {
    JSON,
    CSV,
    TSV,
    XML,
    TBX,
    XLSX
}

/**
 * Options for applying glossary to text.
 */
@Serializable
data class ApplyOptions(
    /** Whether to highlight replacements */
    val highlightReplacements: Boolean = false,
    /** Whether to show original in tooltip */
    val showOriginalInTooltip: Boolean = true,
    /** Maximum replacements per term */
    val maxReplacementsPerTerm: Int = -1,
    /** Whether to apply case-insensitively */
    val caseInsensitive: Boolean = true,
    /** Whether to match whole words only */
    val wholeWordOnly: Boolean = true
)

/**
 * Result of applying glossary to text.
 */
@Serializable
data class GlossaryApplyResult(
    /** Processed text */
    val processedText: String,
    /** Number of replacements made */
    val replacementCount: Int,
    /** Replacements made */
    val replacements: List<Replacement> = emptyList()
)

@Serializable
data class Replacement(
    /** Original term */
    val original: String,
    /** Replacement value */
    val replacement: String,
    /** Start position in original text */
    val startIndex: Int,
    /** End position in original text */
    val endIndex: Int,
    /** Entry ID that matched */
    val entryId: String
)

/**
 * Glossary statistics.
 */
@Serializable
data class GlossaryStatistics(
    /** Glossary ID */
    val glossaryId: String,
    /** Total entries */
    val totalEntries: Int,
    /** Entries with aliases */
    val entriesWithAliases: Int,
    /** Entries with examples */
    val entriesWithExamples: Int,
    /** Most used entries */
    val mostUsedEntries: List<String> = emptyList(),
    /** Recently added entries */
    val recentlyAddedCount: Int = 0,
    /** Storage size in bytes */
    val storageSizeBytes: Long
)

/**
 * Result wrapper for glossary operations.
 */
sealed class GlossaryResult<out T> {
    data class Success<T>(val data: T) : GlossaryResult<T>()
    data class Error(val error: GlossaryError) : GlossaryResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    inline fun <R> map(transform: (T) -> R): GlossaryResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

/**
 * Glossary errors.
 */
@Serializable
sealed class GlossaryError {
    data class GlossaryNotFound(val glossaryId: String) : GlossaryError()
    data class EntryNotFound(val entryId: String) : GlossaryError()
    data class DuplicateEntry(val term: String) : GlossaryError()
    data class ImportFailed(val reason: String) : GlossaryError()
    data class ExportFailed(val reason: String) : GlossaryError()
    data class InvalidFormat(val format: String, val reason: String) : GlossaryError()
    data class StorageFull(val maxEntries: Int) : GlossaryError()
    data class ValidationFailed(val field: String, val reason: String) : GlossaryError()
    data class Unknown(val message: String) : GlossaryError()
}
