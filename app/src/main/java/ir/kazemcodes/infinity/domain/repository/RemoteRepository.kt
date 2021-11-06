package ir.kazemcodes.infinity.domain.repository

import ir.kazemcodes.infinity.data.remote.source.model.Book

interface RemoteRepository {

    suspend fun getBookDetail(page : Int) : Book
    suspend fun getLatestBooks(page : Int) : List<Book>
    suspend fun getPopularBooks(page : Int) : List<Book>

}