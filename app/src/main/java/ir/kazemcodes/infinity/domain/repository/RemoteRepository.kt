package ir.kazemcodes.infinity.domain.repository

import android.content.Context
import android.webkit.WebView
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
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