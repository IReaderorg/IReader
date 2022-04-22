package org.ireader.domain.use_cases.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import org.ireader.core.exceptions.EmptyQuery
import org.ireader.core.utils.UiText
import org.ireader.core.utils.exceptionHandler
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.model.Filter
import org.ireader.core_api.source.model.Listing
import org.ireader.core_api.source.model.MangasPageInfo
import javax.inject.Inject

class GetRemoteBooksUseCase @Inject constructor(@ApplicationContext private val context: Context)  {
    suspend operator fun invoke(
        query: String? = null,
        listing: Listing?,
        filters: List<Filter<*>>?,
        source: CatalogSource,
        page: Int,
        onError: suspend (UiText?) -> Unit,
        onSuccess: suspend (MangasPageInfo) -> Unit,
    ) {

        try {

            var item: MangasPageInfo = MangasPageInfo(emptyList(), false)

            if (query != null) {
                if (query != null && query.isNotBlank()) {
                    item = source.getMangaList(filters = listOf(Filter.Title()
                        .apply { this.value = query }), page = page)
                } else {
                    throw EmptyQuery()
                }
            } else if (filters != null) {
                item = source.getMangaList(filters = filters, page)
            } else {
                item = source.getMangaList(sort = listing, page)
            }
            onSuccess(item.copy(mangas = item.mangas.filter { it.title.isNotBlank() }))
        } catch (e: CancellationException) {
        } catch (e: Exception) {
            onError(exceptionHandler(e))
        }
    }
}
