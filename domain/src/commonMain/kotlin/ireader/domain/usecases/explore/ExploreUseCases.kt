package ireader.domain.usecases.explore

import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.local.OpenLocalFolder
import ireader.domain.usecases.local.book_usecases.FindDuplicateBook
import ireader.domain.usecases.preferences.reader_preferences.BrowseScreenPrefUseCase
import ireader.domain.usecases.remote.RemoteUseCases

/**
 * Aggregate for all explore/browse-related use cases.
 * Reduces ExploreViewModel constructor params from 10 to 3.
 * 
 * Usage:
 * ```kotlin
 * class ExploreViewModel(
 *     private val exploreUseCases: ExploreUseCases,
 *     // ... other deps
 * ) {
 *     suspend fun searchBooks(query: String) {
 *         exploreUseCases.remote.getRemoteBooks(...)
 *     }
 * }
 * ```
 * 
 * Requirements: 4.2 - ExploreViewModel accepts ExploreUseCases aggregate
 */
data class ExploreUseCases(
    val remote: RemoteUseCases,
    val insert: LocalInsertUseCases,
    val findDuplicate: FindDuplicateBook,
    val openLocalFolder: OpenLocalFolder,
    val browseScreenPref: BrowseScreenPrefUseCase
)
