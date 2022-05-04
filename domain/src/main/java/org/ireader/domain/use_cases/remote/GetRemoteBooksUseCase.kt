package org.ireader.domain.use_cases.remote

import org.ireader.common_extensions.withIOContext
import org.ireader.core.exceptions.EmptyQuery
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.model.Filter
import org.ireader.core_api.source.model.Listing
import org.ireader.core_api.source.model.MangasPageInfo
import javax.inject.Inject

class GetRemoteBooksUseCase @Inject constructor() {
    suspend operator fun invoke(
        query: String? = null,
        listing: Listing?,
        filters: List<Filter<*>>?,
        source: CatalogSource,
        page: Int,
        onError: suspend (Throwable) -> Unit,
        onSuccess: suspend (MangasPageInfo) -> Unit,
    ) {
        withIOContext {
            try {
                var item: MangasPageInfo = MangasPageInfo(emptyList(), false)
                if (query != null) {
                    if (query != null && query.isNotBlank()) {
                        item = source.getMangaList(
                            filters = listOf(
                                Filter.Title()
                                    .apply { this.value = query }
                            ),
                            page = page
                        )
                    } else {
                        throw EmptyQuery()
                    }
                } else if (filters != null) {
                    item = source.getMangaList(filters = filters, page)
                } else {
                    item = source.getMangaList(sort = listing, page)
                }
                onSuccess(item.copy(mangas = item.mangas.filter { it.title.isNotBlank() }))
            }catch (e:Error ) {
                onError(e)
            }catch (e:Throwable) {
                onError(e)
            }
        }
    }
}
