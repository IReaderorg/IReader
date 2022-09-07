package ireader.domain.use_cases.epub

import android.content.Context
import android.net.Uri
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.MediaType
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubWriter
import nl.siegmann.epublib.service.MediatypeService
import ireader.common.data.repository.ChapterRepository
import ireader.common.models.entities.Book
import ireader.ui.imageloader.BookCover
import ireader.ui.imageloader.coil.cache.CoverCache
import org.koin.core.annotation.Factory
import java.io.FileOutputStream

@Factory
class EpubCreator(
    private val coverCache: CoverCache,
    private val chapterRepository: ChapterRepository
) {

    suspend operator fun invoke(book: Book, uri: Uri, context: Context) {
        val epubBook = nl.siegmann.epublib.domain.Book()
        val chapters = chapterRepository.findChaptersByBookId(book.id)
        val metadata = epubBook.metadata
        metadata.addAuthor(Author(book.author ?: ""))
        metadata.addTitle(book.title)
        metadata.addPublisher("IReader")
        val cover = coverCache.getCoverFile(BookCover.Companion.from(book))
        if (cover?.exists() == true) {
            epubBook.coverImage = Resource(cover.readBytes(), MediaType("cover", ".jpg"))
        }

        chapters.forEachIndexed { index, chapter ->
            val resource: Resource = Resource("$index", chapter.content.map { "<p>$it</p>" }.joinToString("\n").toByteArray(), "${chapter.name}-$index.html", MediatypeService.XHTML)
            epubBook.addSection(chapter.name, resource)
        }
        writeToUri(uri, context, epubBook)
    }
    private fun writeToUri(uri: Uri, context: Context, book: nl.siegmann.epublib.domain.Book) {
        val contentResolver = context.contentResolver
        val pfd = contentResolver.openFileDescriptor(uri, "w") ?: return
        FileOutputStream(pfd.fileDescriptor).use {
            EpubWriter().write(book, it)
        }
    }
}
