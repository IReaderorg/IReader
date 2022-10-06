package ireader.domain.usecases.remote

import ireader.domain.models.entities.CatalogLocal
import ireader.i18n.EmptyQuery
import ireader.i18n.SourceNotFoundException
import ireader.core.source.model.Filter
import ireader.core.source.model.Listing
import ireader.core.source.model.MangasPageInfo
import ireader.domain.utils.extensions.async.withIOContext
import org.koin.core.annotation.Factory

@Factory
class GetRemoteBooksUseCase() {
    suspend operator fun invoke(
        query: String? = null,
        listing: Listing?,
        filters: List<Filter<*>>?,
        catalog: CatalogLocal?,
        page: Int,
        onError: suspend (Throwable) -> Unit,
        onSuccess: suspend (MangasPageInfo) -> Unit,
    ) {
        val source = catalog?.source ?: throw SourceNotFoundException()
        if (source !is ireader.core.source.CatalogSource) throw SourceNotFoundException()
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
            } catch (e: Error) {
                onError(e)
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }
}
