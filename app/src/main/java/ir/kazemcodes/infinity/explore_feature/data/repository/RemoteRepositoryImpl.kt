package ir.kazemcodes.infinity.explore_feature.data.repository

//class RemoteRepositoryImpl (
//    private val api : ParsedHttpSource
//) : RemoteRepository {
//    override suspend fun getElements(url: String, headers: Map<String, String>): Elements {
//        return api.fetchElements(url = url, headers = headers)
//    }
//
//
//    override suspend fun getBooks(elements: Elements): List<Book> {
//        return api.fetchBooks(elements = elements)
//    }
//
//    override suspend fun getBookDetail(book: Book, elements: Elements): Book {
//        return api.fetchBook(book , elements)
//    }
//
//    override suspend fun getChapters(book: Book, elements: Elements): List<Chapter> {
//        return api.fetchChapters(book,elements)
//    }
//
//    override suspend fun getReadingContent(elements: Elements): String {
//        return api.fetchReadingContent(elements = elements)
//    }
//
//}