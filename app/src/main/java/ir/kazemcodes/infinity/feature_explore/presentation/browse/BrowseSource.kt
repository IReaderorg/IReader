package ir.kazemcodes.infinity.feature_explore.presentation.browse

//class BrowseSource(private val source: Source,private val exploreType: ExploreType) : PagingSource<Int, Book>() {
//
//    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
//        return state.anchorPosition
//    }
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
//
//        val page = params.key ?: 1
//
//        return try {
//            source.fetchLatest(page)
//            val response = when (exploreType) {
//                is ExploreType.Latest ->{
//                    source.fetchLatest(page)
//                }
//                is ExploreType.Popular -> {
//                    source.fetchPopular(page)
//                }
//            }
//            LoadResult.Page(
//                data = response.books,
//                prevKey = if (page == 1) null else page - 1,
//                nextKey = if (response.books.isEmpty()) null else page + 1
//            )
//
//        } catch (e: IOException) {
//            LoadResult.Error(e)
//        } catch (e: HttpException) {
//            LoadResult.Error(e)
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//}