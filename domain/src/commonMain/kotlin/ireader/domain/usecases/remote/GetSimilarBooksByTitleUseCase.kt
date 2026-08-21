package ireader.domain.usecases.remote

import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.model.Filter
import ireader.core.source.model.FilterList
import ireader.core.source.model.MangaInfo
import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Recommendation
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.utils.exceptionHandler
import ireader.i18n.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

class GetSimilarBooksByTitleUseCase(
    private val catalogStore: CatalogStore
) {
    suspend operator fun invoke(
        book: Book,
        onError: suspend (UiText?) -> Unit,
        onSuccess: suspend (List<Recommendation>) -> Unit,
        maxResults: Int = 10,
        sourceFilter: PreferenceValues.SimilarTitlesSource = PreferenceValues.SimilarTitlesSource.SameSource
    ) {
        kotlin.runCatching {
            when (sourceFilter) {
                PreferenceValues.SimilarTitlesSource.SameSource -> {
                    searchSameSource(book, maxResults, onSuccess)
                }
                PreferenceValues.SimilarTitlesSource.OtherSources,
                PreferenceValues.SimilarTitlesSource.AllSources -> {
                    searchCrossSource(book, maxResults, sourceFilter, onSuccess)
                }
            }
        }.getOrElse { e ->
            if (e !is CancellationException) {
                onError(exceptionHandler(e))
            }
        }
    }

    private suspend fun searchSameSource(
        book: Book,
        maxResults: Int,
        onSuccess: suspend (List<Recommendation>) -> Unit
    ) {
        val catalog = catalogStore.get(book.sourceId)
        val source = catalog?.source as? CatalogSource ?: return
        
        if (!source.supportsSearch()) {
            Log.debug { "Source ${catalog.name} does not support search" }
            return
        }
        
        val keywords = extractKeywords(book.title)
        if (keywords.isEmpty()) {
            Log.debug { "No keywords extracted from title: ${book.title}" }
            return
        }
        
        val seenKeys = mutableSetOf<String>()
        val results = mutableListOf<MangaInfo>()
        
        for (keyword in keywords) {
            if (results.size >= maxResults) break
            
            try {
                val filters: FilterList = listOf(Filter.Title().apply { value = keyword })
                val pageInfo = source.getMangaList(filters, page = 1)
                
                for (manga in pageInfo.mangas) {
                    if (results.size >= maxResults) break
                    if (manga.key == book.key) continue
                    if (manga.key in seenKeys) continue
                    
                    seenKeys.add(manga.key)
                    results.add(manga)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.warn { "Search failed for keyword '$keyword': ${e.message}" }
            }
        }
        
        if (results.isEmpty()) {
            try {
                val listings = source.getListings()
                val listing = listings.firstOrNull()
                val pageInfo = source.getMangaList(listing, 1)
                
                for (manga in pageInfo.mangas) {
                    if (results.size >= maxResults) break
                    if (manga.key == book.key) continue
                    if (manga.key in seenKeys) continue
                    
                    seenKeys.add(manga.key)
                    results.add(manga)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.warn { "Random fallback failed: ${e.message}" }
            }
        }
        
        val recommendations = results.map { manga ->
            Recommendation(
                key = manga.key,
                title = manga.title,
                cover = manga.cover,
                genres = manga.genres,
                sourceId = book.sourceId,
                sourceName = catalog.name
            )
        }
        
        onSuccess(recommendations)
    }
    
    private suspend fun searchCrossSource(
        book: Book,
        maxResults: Int,
        sourceFilter: PreferenceValues.SimilarTitlesSource,
        onSuccess: suspend (List<Recommendation>) -> Unit
    ) {
        val keywords = extractKeywords(book.title)
        if (keywords.isEmpty()) {
            Log.debug { "No keywords extracted from title: ${book.title}" }
            searchSameSource(book, maxResults, onSuccess)
            return
        }
        
        val catalogs = catalogStore.getCatalogsFlow().first { it.isNotEmpty() }
        
        val targetSources = when (sourceFilter) {
            PreferenceValues.SimilarTitlesSource.OtherSources -> {
                catalogs.filter { it.sourceId != book.sourceId }
            }
            PreferenceValues.SimilarTitlesSource.AllSources -> {
                catalogs
            }
            else -> emptyList()
        }
        
        if (targetSources.isEmpty()) {
            Log.debug { "No sources available for cross-source search, falling back to same source" }
            searchSameSource(book, maxResults, onSuccess)
            return
        }
        
        val searchableSources = targetSources.count { it.source is CatalogSource && (it.source as CatalogSource).supportsSearch() }
        Log.debug { "Cross-source search: ${targetSources.size} sources, $searchableSources searchable" }
        
        val seenKeys = mutableSetOf<String>()
        val results = mutableListOf<Pair<ireader.domain.models.entities.CatalogLocal, MangaInfo>>()
        val maxPerSource = maxResults.coerceAtLeast(1)
        
        coroutineScope {
            val nonJsResults = targetSources
                .filter { it !is ireader.domain.models.entities.JSPluginCatalog }
                .map { catalog ->
                    async {
                        searchSourceForKeywords(catalog, keywords, book, maxPerSource, seenKeys)
                    }
                }.awaitAll()
            
            val jsResults = targetSources
                .filter { it is ireader.domain.models.entities.JSPluginCatalog }
                .map { catalog ->
                    searchSourceForKeywords(catalog, keywords, book, maxPerSource, seenKeys)
                }
            
            val allResults = nonJsResults.flatten() + jsResults.flatten()
            results.addAll(allResults.take(maxResults))
        }
        
        if (results.isEmpty()) {
            Log.debug { "Cross-source search returned no results, falling back to same source" }
            searchSameSource(book, maxResults, onSuccess)
            return
        }
        
        val recommendations = results.map { (catalog, manga) ->
            Recommendation(
                key = manga.key,
                title = manga.title,
                cover = manga.cover,
                genres = manga.genres,
                sourceId = catalog.sourceId,
                sourceName = catalog.name
            )
        }
        
        onSuccess(recommendations)
    }
    
    private suspend fun searchSourceForKeywords(
        catalog: ireader.domain.models.entities.CatalogLocal,
        keywords: List<String>,
        book: Book,
        maxResults: Int,
        seenKeys: MutableSet<String>
    ): List<Pair<ireader.domain.models.entities.CatalogLocal, MangaInfo>> {
        val source = catalog.source as? CatalogSource ?: return emptyList()
        
        if (!source.supportsSearch()) {
            Log.debug { "Source ${catalog.name} does not support search" }
            return emptyList()
        }
        
        val sourceResults = mutableListOf<Pair<ireader.domain.models.entities.CatalogLocal, MangaInfo>>()
        
        for (keyword in keywords) {
            if (sourceResults.size >= maxResults) break
            
            try {
                val filters: FilterList = listOf(Filter.Title().apply { value = keyword })
                val pageInfo = source.getMangaList(filters, page = 1)
                
                for (manga in pageInfo.mangas) {
                    if (sourceResults.size >= maxResults) break
                    if (manga.key == book.key) continue
                    if (manga.key in seenKeys) continue
                    
                    seenKeys.add(manga.key)
                    sourceResults.add(catalog to manga)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.warn { "Cross-source search failed for keyword '$keyword' in ${catalog.name}: ${e.message}" }
            }
        }
        
        if (sourceResults.isEmpty()) {
            try {
                val listings = source.getListings()
                val listing = listings.firstOrNull()
                val pageInfo = source.getMangaList(listing, 1)
                
                for (manga in pageInfo.mangas) {
                    if (sourceResults.size >= maxResults) break
                    if (manga.key == book.key) continue
                    if (manga.key in seenKeys) continue
                    
                    seenKeys.add(manga.key)
                    sourceResults.add(catalog to manga)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.warn { "Cross-source random fallback failed for ${catalog.name}: ${e.message}" }
            }
        }
        
        return sourceResults
    }
    
    private fun extractKeywords(title: String): List<String> {
        val stopWords = setOf(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "were", "been",
            "be", "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "shall", "can", "need", "dare", "ought", "used",
            "de", "la", "el", "los", "las", "un", "una", "unos", "unas", "y", "o",
            "en", "con", "por", "para", "del", "al", "que", "se", "no", "si", "ya",
            "le", "me", "te", "lo", "la", "les", "nos", "vos", "ellos", "ellas",
            "von", "der", "die", "das", "und", "oder", "ist", "sind", "war", "waren",
            "ein", "eine", "einer", "eines", "dem", "den", "des", "zu", "auf", "in",
            "part", "vol", "chapter", "chapters", "novel", "manga", "manhwa", "manhua",
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "were", "been"
        )
        
        return title.split(Regex("\\s+"))
            .map { it.trim().lowercase().removeSuffix(".").removeSuffix(",") }
            .filter { it.length > 2 && it !in stopWords }
            .distinct()
            .take(5)
    }
}
