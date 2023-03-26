package ireader.domain.usecases.epub

import android.content.Context
import android.content.Intent
import android.net.Uri
import ireader.core.source.model.Text
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.image.cache.CoverCache
import ireader.domain.models.BookCover
import ireader.domain.models.entities.Book
import ireader.domain.utils.fastForEachIndexed
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.MediaType
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubWriter
import nl.siegmann.epublib.service.MediatypeService
import java.io.FileOutputStream


actual class EpubCreator(
    private val coverCache: CoverCache,
    private val chapterRepository: ChapterRepository,
    private val context: Context
) {

    private fun writeToUri(uri: Uri, book: nl.siegmann.epublib.domain.Book) {
        val contentResolver = context.contentResolver
        val pfd = contentResolver.openFileDescriptor(uri, "w") ?: return
        FileOutputStream(pfd.fileDescriptor).use {
            EpubWriter().write(book, it)
        }
    }

    actual suspend operator fun invoke(book: Book, uri: ireader.domain.models.common.Uri,currentEvent: (String) -> Unit) {
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

        chapters.fastForEachIndexed { index, chapter ->
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
        writeToUri(uri.androidUri, epubBook)
    }
    private val reservedChars = "|\\?*<\":>+[]/'"
    private fun sanitizeFilename(name: String): String {
        var tempName = name
        for (c in reservedChars) {
            tempName = tempName.replace(c, ' ')
        }
        return tempName.replace("  ", " ")
    }
    actual fun onEpubCreateRequested(book: Book, onStart: (Any) -> Unit) {
        val mimeTypes = arrayOf("application/epub+zip")
        val fn = "${sanitizeFilename(book.title)}.epub"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/epub+zip")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                .putExtra(
                        Intent.EXTRA_TITLE, fn
                )
        onStart(intent)
    }
}
