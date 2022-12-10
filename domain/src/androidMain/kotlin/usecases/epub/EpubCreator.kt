package ireader.domain.usecases.epub

import android.content.Context
import android.net.Uri
import ireader.domain.models.entities.Book
import ireader.core.source.model.Text
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.image.cache.CoverCache
import ireader.domain.models.BookCover
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.MediaType
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubWriter
import nl.siegmann.epublib.service.MediatypeService
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
            val contents = chapter.content.mapNotNull {
                when(it) {
                    is Text -> it.text
                    else -> null
                }
            }
            val resource: Resource = Resource("$index", contents.joinToString("\n") { "<p>$it</p>" }
                .toByteArray(), "${chapter.name}-$index.html", MediatypeService.XHTML)
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
