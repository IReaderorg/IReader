package ir.kazemcodes.infinity.feature_library.data.paging

//class LibraryScreenSource(
//    private val repository: Repository
//) : PagingSource<Int, Book>() {
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
//        return try {
//            val nextPage = params.key ?: 1
//            val response = repository.localBookRepository.getAllBooksByPaging()
//
//            LoadResult.Page(
//                data = emptyList(),
//                prevKey = if (nextPage == 1) null else nextPage - 1,
//                nextKey = response.page.plus(1)
//            )
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
//        TODO("Not yet implemented")
//    }
//}