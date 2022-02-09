package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.book_usecases.GetAllInDownloadsPagingData
import org.ireader.domain.use_cases.local.book_usecases.GetAllInLibraryBooks
import org.ireader.domain.use_cases.local.book_usecases.GetAllInLibraryPagingSource
import org.ireader.domain.use_cases.local.book_usecases.GetBookById
import org.ireader.infinity.core.domain.use_cases.local.book_usecases.*

data class LocalGetBookUseCases(
    val getBookById: GetBookById,
    val GetInLibraryBooksPagingData: GetInLibraryBooksPagingData,
    val getAllInLibraryPagingSource: GetAllInLibraryPagingSource,
    val getAllInLibraryBooks: GetAllInLibraryBooks,
    val getBooksByQueryByPagination: GetBooksByQueryByPagination,
    val getBooksByQueryPagingSource: GetBooksByQueryPagingSource,
    val getAllExploredBookPagingSource: GetAllExploredBookPagingSource,
    val getAllExploredBookPagingData: GetAllExploredBookPagingData,
    val getAllInDownloadsPagingData: GetAllInDownloadsPagingData,
)















