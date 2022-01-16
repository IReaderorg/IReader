package ir.kazemcodes.infinity.core.domain.repository

import android.content.Context
import android.webkit.WebView
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import kotlinx.coroutines.flow.Flow

interface RemoteRepository {


    suspend fun downloadChapter(
        book: Book,
        source: Source,
        chapters: List<Chapter>,
        factory: (Context) -> WebView,
        totalRetries: Int = 3,
    ): Flow<Chapter>


}