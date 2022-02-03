package ir.kazemcodes.infinity.core.domain.use_cases.local

import ir.kazemcodes.infinity.core.domain.use_cases.local.book_usecases.*

data class LocalGetBookUseCases(
    val getBookById: GetBookById,
    val GetInLibraryBooksPagingData: GetInLibraryBooksPagingData,
    val getAllInLibraryPagingSource: GetAllInLibraryPagingSource,
    val getAllInLibraryBooks: GetAllInLibraryBooks,
    val getBooksByQueryByPagination: GetBooksByQueryByPagination,
    val getBooksByQueryPagingSource: GetBooksByQueryPagingSource,
    val getAllExploredBookPagingSource: GetAllExploredBookPagingSource,
    val getAllExploredBookPagingData: GetAllExploredBookPagingData,
    val getAllInDownloadsPagingData: GetAllInDownloadsPagingData
)















