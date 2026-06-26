package ireader.data.catalog.impl.tsundoku

import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import ireader.core.source.model.PageUrl
import ireader.core.source.model.Text

/**
 * Adapter utilities for converting between Tsundoku (Tachiyomi/Mihon) source models
 * and IReader source models.
 *
 * Tsundoku uses: eu.kanade.tachiyomi.source.model.{SManga, SChapter, Page, MangasPage}
 * IReader uses:   ireader.core.source.model.{MangaInfo, ChapterInfo, Page, MangasPageInfo}
 */
object TsundokuModelAdapter {

    // ==================== Manga ====================

    /**
     * Convert a Tsundoku SManga to an IReader MangaInfo.
     */
    fun Any.toMangaInfo(): MangaInfo {
        val smanga = this
        val url = smanga.getField("url") as? String ?: ""
        val title = smanga.getField("title") as? String ?: ""
        val thumbnailUrl = smanga.getField("thumbnail_url") as? String
        val artist = smanga.getField("artist") as? String ?: ""
        val author = smanga.getField("author") as? String ?: ""
        val description = smanga.getField("description") as? String ?: ""
        val genre = smanga.getField("genre") as? String
        val status = smanga.getField("status") as? Int ?: 0

        return MangaInfo(
            key = url,
            title = title,
            artist = artist,
            author = author,
            description = description,
            genres = genre?.split(Regex("[,;|]+"))?.map { it.trim() }?.filter { it.isNotBlank() }?.distinct() ?: emptyList(),
            status = status.toLong(),
            cover = thumbnailUrl ?: ""
        )
    }

    /**
     * Convert an IReader MangaInfo to a Tsundoku-compatible SManga.
     * Returns an SMangaImpl populated with MangaInfo data.
     */
    fun MangaInfo.toTsundokuSManga(): Any {
        val smangaClass = try {
            Class.forName("eu.kanade.tachiyomi.source.model.SMangaImpl")
        } catch (e: ClassNotFoundException) {
            Class.forName("eu.kanade.tachiyomi.source.model.SManga\$Companion")
        }

        // Try to create via SManga.create() companion method
        val smangaCompanion = try {
            val smangaClass2 = Class.forName("eu.kanade.tachiyomi.source.model.SManga")
            smangaClass2.getMethod("create").invoke(null)
        } catch (e: Exception) {
            // Fallback: try SMangaImpl direct instantiation
            smangaClass.getDeclaredConstructor().newInstance()
        }

        smangaCompanion.setField("url", this.key)
        smangaCompanion.setField("title", this.title)
        smangaCompanion.setField("thumbnail_url", this.cover.ifBlank { null })
        smangaCompanion.setField("artist", this.artist)
        smangaCompanion.setField("author", this.author)
        smangaCompanion.setField("description", this.description)
        smangaCompanion.setField("genre", this.genres.joinToString(", "))
        smangaCompanion.setField("status", this.status.toInt())
        smangaCompanion.setField("initialized", true)

        return smangaCompanion
    }

    // ==================== Chapter ====================

    /**
     * Convert a Tsundoku SChapter to an IReader ChapterInfo.
     */
    fun Any.toChapterInfo(): ChapterInfo {
        val schapter = this
        val url = schapter.getField("url") as? String ?: ""
        val name = schapter.getField("name") as? String ?: ""
        val chapterNumber = schapter.getField("chapter_number") as? Float ?: -1f
        val dateUpload = schapter.getField("date_upload") as? Long ?: 0L
        val scanlator = schapter.getField("scanlator") as? String ?: ""

        return ChapterInfo(
            key = url,
            name = name,
            dateUpload = dateUpload,
            number = chapterNumber,
            scanlator = scanlator,
            type = ChapterInfo.NOVEL
        )
    }

    /**
     * Convert an IReader ChapterInfo to a Tsundoku-compatible SChapter.
     */
    fun ChapterInfo.toTsundokuSChapter(): Any {
        val schapterClass = try {
            Class.forName("eu.kanade.tachiyomi.source.model.SChapter")
        } catch (e: ClassNotFoundException) {
            null
        }

        val schapter = try {
            schapterClass?.getMethod("create")?.invoke(null)
                ?: Class.forName("eu.kanade.tachiyomi.source.model.SChapterImpl").getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            Class.forName("eu.kanade.tachiyomi.source.model.SChapterImpl").getDeclaredConstructor().newInstance()
        }

        schapter.setField("url", this.key)
        schapter.setField("name", this.name)
        schapter.setField("chapter_number", this.number)
        schapter.setField("date_upload", this.dateUpload)
        schapter.setField("scanlator", this.scanlator.ifBlank { null })

        return schapter
    }

    // ==================== Page ====================

    /**
     * Convert a Tsundoku Page to IReader Page list.
     * Tsundoku Page has: index, url, imageUrl, text
     * IReader Page is a sealed class: PageUrl, ImageUrl, Text, MovieUrl, etc.
     */
    fun Any.toIReaderPages(): List<Page> {
        val page = this
        val url = page.getField("url") as? String ?: ""
        val imageUrl = page.getField("imageUrl") as? String
        val text = page.getField("text") as? String

        val pages = mutableListOf<Page>()

        // If there's text content (novel source), return as Text page
        if (!text.isNullOrBlank()) {
            pages.add(Text(text))
        }

        // If there's an image URL, return as ImageUrl
        if (!imageUrl.isNullOrBlank()) {
            pages.add(ImageUrl(imageUrl))
        } else if (url.isNotBlank()) {
            // Fall back to page URL
            pages.add(PageUrl(url))
        }

        return pages
    }

    /**
     * Convert an IReader Page to a Tsundoku Page.
     */
    fun Page.toTsundokuPage(index: Int = 0): Any {
        val pageClass = Class.forName("eu.kanade.tachiyomi.source.model.Page")
        val page = pageClass.getDeclaredConstructor(Int::class.java, String::class.java, String::class.java)
            .newInstance(index, getUrl(this), getImageUrl(this))

        // Set text content for novel pages
        val textContent = getTextContent(this)
        if (textContent != null) {
            page.setField("text", textContent)
        }

        return page
    }

    // ==================== MangasPage ====================

    /**
     * Convert a Tsundoku MangasPage to an IReader MangasPageInfo.
     */
    fun Any.toMangasPageInfo(): MangasPageInfo {
        val mangasPage = this
        @Suppress("UNCHECKED_CAST")
        val mangas = mangasPage.getField("mangas") as? List<Any> ?: emptyList()
        val hasNextPage = mangasPage.getField("hasNextPage") as? Boolean ?: false

        return MangasPageInfo(
            mangas = mangas.map { it.toMangaInfo() },
            hasNextPage = hasNextPage
        )
    }

    /**
     * Convert an IReader MangasPageInfo to a Tsundoku MangasPage.
     */
    fun MangasPageInfo.toTsundokuMangasPage(): Any {
        val mangasPageClass = Class.forName("eu.kanade.tachiyomi.source.model.MangasPage")
        val smangaList = this.mangas.map { it.toTsundokuSManga() }

        return mangasPageClass.getDeclaredConstructor(
            List::class.java,
            Boolean::class.java
        ).newInstance(smangaList, this.hasNextPage)
    }

    // ==================== Status Mapping ====================

    /**
     * Tsundoku and IReader use the same status constants:
     * UNKNOWN=0, ONGOING=1, COMPLETED=2, LICENSED=3, PUBLISHING_FINISHED=4, CANCELLED=5, ON_HIATUS=6
     * But Tsundoku uses Int, IReader uses Long.
     */
    fun Int.toIReaderStatus(): Long = this.toLong()
    fun Long.toTsundokuStatus(): Int = this.toInt()

    // ==================== Reflection Helpers ====================

    private fun Any.getField(name: String): Any? {
        return try {
            var clazz: Class<*>? = this.javaClass
            while (clazz != null) {
                try {
                    val field = clazz.getDeclaredField(name)
                    field.isAccessible = true
                    return field.get(this)
                } catch (e: NoSuchFieldException) {
                    clazz = clazz.superclass
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun Any.setField(name: String, value: Any?) {
        try {
            var clazz: Class<*>? = this.javaClass
            while (clazz != null) {
                try {
                    val field = clazz.getDeclaredField(name)
                    field.isAccessible = true
                    field.set(this, value)
                    return
                } catch (e: NoSuchFieldException) {
                    clazz = clazz.superclass
                }
            }
        } catch (e: Exception) {
            // Silently fail - some fields may be read-only
        }
    }

    private fun getUrl(page: Page): String = when (page) {
        is PageUrl -> page.url
        is ImageUrl -> page.url
        is Text -> ""
        else -> ""
    }

    private fun getImageUrl(page: Page): String? = when (page) {
        is ImageUrl -> page.url
        else -> null
    }

    private fun getTextContent(page: Page): String? = when (page) {
        is Text -> page.text
        else -> null
    }
}
