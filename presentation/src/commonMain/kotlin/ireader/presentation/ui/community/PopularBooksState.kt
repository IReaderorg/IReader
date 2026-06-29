package ireader.presentation.ui.community

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.domain.models.gamification.CommunityAnnouncement
import ireader.domain.models.entities.SourceGroup
import ireader.domain.models.remote.PopularBook

/**
 * One source where a popular book is available, with its reader count.
 */
@Immutable
data class BookSourceVariant(
    val sourceId: Long,
    val sourceName: String,
    val bookId: String,
    val bookUrl: String,
    val readers: Int,
)

/**
 * A deduped popular book: a single title aggregating every source it appears on.
 * `totalReaders` sums across sources; `sources` is sorted most-popular first.
 */
@Immutable
data class PopularBookGroup(
    val key: String,                 // normalized title key
    val title: String,
    val coverUrl: String?,
    val description: String?,
    val totalReaders: Int,
    val lastRead: Long,
    val localBookId: Long?,
    val sources: List<BookSourceVariant>,
) {
    val primary: BookSourceVariant get() = sources.first()
    val sourceCount: Int get() = sources.size
}

/**
 * Immutable state for the Popular Books screen following Mihon's StateScreenModel pattern.
 */
@Immutable
data class PopularBooksScreenState(
    val books: List<PopularBook> = emptyList(),
    val groups: List<PopularBookGroup> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRateLimited: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val error: String? = null,
    val loadingBookIds: Set<String> = emptySet(),
    val votedBookIds: Set<String> = emptySet(),
    val announcements: List<CommunityAnnouncement> = emptyList(),
    val discordOnline: Int? = null,
    val selectedBook: PopularBookGroup? = null,        // book detail sheet
    val resolvingSourceFor: String? = null,            // group key currently resolving
    val showSourceInstallDialog: Boolean = false,
    val pendingInstallSourceName: String = "",
    val pendingInstallSourceGroup: SourceGroup? = null,
) {
    val isEmpty: Boolean get() = groups.isEmpty() && !isInitialLoading

    val isInitialLoadingState: Boolean get() = isInitialLoading && groups.isEmpty()

    val hasContent: Boolean get() = groups.isNotEmpty()
}

/**
 * Navigation actions for book items
 */
sealed class BookNavigationAction {
    data class OpenLocalBook(val bookId: Long) : BookNavigationAction()
    data class OpenGlobalSearch(val query: String) : BookNavigationAction()
    data class OpenExternalUrl(val url: String) : BookNavigationAction()
    /** Source for the chosen variant isn't installed; prompt the user. */
    data class SourceMissing(val sourceName: String) : BookNavigationAction()
}
