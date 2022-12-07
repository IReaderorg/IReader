// package ireader.domain.use_cases.epub
//
// import android.content.Context
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.async
// import kotlinx.coroutines.withContext
// import org.ireader.common_data.repository.BookRepository
// import org.ireader.common_data.repository.ChapterRepository
// import ireader.common.models.entities.Book
// import ireader.domain.models.entities.Chapter
// import ireader.core.source.LocalSource
// import ir.kazemcodes.epub.model.EpubBook
// import java.io.File
//
//
// class ImportEpub(
//    private val bookRepository: BookRepository,
//    private val chapterRepository: ChapterRepository,
// ) {
//    suspend operator fun invoke(epub: EpubBook, context: Context) {
//        // First clean any previous entries from the book
//        fun String.withLocalPrefix() = "local://${this}"
//        val bookUrl = epub.fileName.withLocalPrefix()
//        val imgFile = File(context.filesDir, "library_covers/${epub.fileName}")
//        bookRepository.delete(bookUrl)
//        // Insert new book data
//        val bookId = Book(title = epub.title, key = bookUrl, favorite = true, sourceId = LocalSource.SOURCE_ID, cover = imgFile.path)
//            .let { bookRepository.insertBook(it) }
//        epub.chapters
//            .mapIndexed { i, it ->
//                Chapter(
//                    name = it.title,
//                    key = it.url.withLocalPrefix(),
//                    bookId = bookId,
//                    content = it.body.filter { it.isNotBlank() },
//                )
//            }
//            .let { chapterRepository.insertChapters(it) }
//        epub.images.map {
//            withContext(Dispatchers.IO) {
//                async {
//                    imgFile.parentFile?.also { parent ->
//                        parent.mkdirs()
//                        if (parent.exists())
//                            imgFile.writeBytes(it.image)
//                    }
//                }
//            }
//        }
//    }
// }
