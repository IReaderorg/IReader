package org.ireader.domain.use_cases.local

import androidx.annotation.Keep
import org.ireader.domain.use_cases.local.delete_usecases.book.*
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteAllChapters
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapterByChapter
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapters
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChaptersByBookId
import org.ireader.domain.use_cases.remote.key.DeleteAllRemoteKeys
import javax.inject.Inject

@Keep
data class DeleteUseCase @Inject constructor(
    val deleteAllExploreBook: DeleteAllExploreBook,
    val deleteBooks: DeleteBooks,
    val deleteBookAndChapterByBookIds: DeleteBookAndChapterByBookIds,
    val deleteAllRemoteKeys: DeleteAllRemoteKeys,
    val deleteBookById: DeleteBookById,
    val deleteAllBook: DeleteAllBooks,
    val deleteChaptersByBookId: DeleteChaptersByBookId,
    val deleteAllChapters: DeleteAllChapters,
    val deleteChapterByChapter: DeleteChapterByChapter,
    val deleteChapters: DeleteChapters,
)

















