package ireader.domain.usecases.local

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import ireader.core.source.LocalSource
import ireader.core.source.LocalCatalogSource
import ireader.core.source.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipFile

/**
 * Desktop implementation of LocalCatalogSource
 */
class LocalSourceImpl(
    private val appDataDir: File
) : LocalCatalogSource {
    
    override val id: Long = LocalSource.SOURCE_ID
    override val name: String = "Local Source"
    override val lang: String = "en"
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private fun getLocalDirectory(): File {
        return File(appDataDir, LocalSource.LOCAL_FOLDER_NAME).apply {
            if (!exists()) mkdirs()
        }
    }
    
    override suspend fun scanLocalNovels(): List<MangaInfo> = withContext(Dispatchers.IO) {
        val localDir = getLocalDirectory()
        val novels = mutableListOf<MangaInfo>()
        
        localDir.listFiles()?.filter { it.isDirectory }?.forEach { novelFolder ->
            try {
                val novelInfo = parseNovelFolder(novelFolder)
                novels.add(novelInfo)
            } catch (_: Exception) {
                // Silently ignore parse errors
            }
        }
        
        novels.sortedBy { it.title }
    }
    
    private fun parseNovelFolder(folder: File): MangaInfo {
        val folderName = folder.name
        val detailsFile = File(folder, "details.json")
        val coverFile = File(folder, "cover.jpg").takeIf { it.exists() }
            ?: File(folder, "cover.png").takeIf { it.exists() }
        
        val details = if (detailsFile.exists()) {
            try {
                json.decodeFromString<LocalNovelDetails>(detailsFile.readText())
            } catch (e: Exception) {
                null
            }
        } else null
        
        return MangaInfo(
            key = "local_${folderName}",
            title = details?.title ?: folderName,
            author = details?.author ?: "",
            artist = details?.artist ?: "",
            description = details?.description ?: "",
            genres = details?.genre ?: emptyList(),
            status = details?.toStatus() ?: MangaInfo.UNKNOWN,
            cover = coverFile?.absolutePath ?: ""
        )
    }
    
    override suspend fun scanNovelChapters(novelKey: String): List<ChapterInfo> = withContext(Dispatchers.IO) {
        val folderName = novelKey.removePrefix("local_")
        val novelFolder = File(getLocalDirectory(), folderName)
        
        if (!novelFolder.exists() || !novelFolder.isDirectory) {
            return@withContext emptyList()
        }
        
        val chapters = mutableListOf<ChapterInfo>()
        
        novelFolder.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in supportedFormats }
            ?.sortedBy { it.name }
            ?.forEachIndexed { index, file ->
                try {
                    val chapterInfo = parseChapterFile(file, novelKey, index)
                    chapters.add(chapterInfo)
                } catch (_: Exception) {
                    // Silently ignore parse errors
                }
            }
        
        chapters
    }
    
    private fun parseChapterFile(file: File, novelKey: String, index: Int): ChapterInfo {
        val fileName = file.nameWithoutExtension
        
        // Use a delimiter that won't appear in folder names
        return ChapterInfo(
            key = "local_${novelKey}|||${file.name}",
            name = fileName,
            dateUpload = file.lastModified(),
            number = index.toFloat(),
            type = ChapterInfo.NOVEL
        )
    }
    
    override suspend fun readChapterFile(chapterKey: String): List<Page> = withContext(Dispatchers.IO) {
        val withoutPrefix = chapterKey.removePrefix("local_local_")
        
        val parts = withoutPrefix.split("|||")
        if (parts.size != 2) {
            return@withContext emptyList()
        }
        
        val folderName = parts[0]
        val fileName = parts[1]
        
        val localDir = getLocalDirectory()
        val novelFolder = File(localDir, folderName)
        val file = File(novelFolder, fileName)
        
        if (!file.exists()) {
            return@withContext emptyList()
        }
        
        when (file.extension.lowercase()) {
            "epub" -> readEpubFile(file)
            "txt" -> readTextFile(file)
            else -> emptyList()
        }
    }
    
    private fun readEpubFile(file: File): List<Page> {
        val pages = mutableListOf<Page>()
        
        try {
            ZipFile(file).use { zip ->
                val opfEntry = findOpfFile(zip) ?: return emptyList()
                val opfContent = zip.getInputStream(opfEntry).bufferedReader().readText()
                val opfDoc = Ksoup.parse(opfContent, Parser.xmlParser())
                
                val opfPath = opfEntry.name
                val basePath = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""
                
                val spineItems = opfDoc.select("spine itemref")
                val manifestItems = opfDoc.select("manifest item").associateBy { it.attr("id") }
                
                spineItems.forEach { itemref ->
                    val idref = itemref.attr("idref")
                    val manifestItem = manifestItems[idref]
                    val href = manifestItem?.attr("href") ?: return@forEach
                    
                    val fullPath = basePath + href
                    val entry = zip.getEntry(fullPath) ?: return@forEach
                    
                    try {
                        val html = zip.getInputStream(entry).bufferedReader().readText()
                        val doc = Ksoup.parse(html)
                        
                        doc.select("h1, h2, h3, h4, h5, h6, p, blockquote, pre, li").forEach { element ->
                            val text = element.text().trim()
                            if (text.isNotBlank()) {
                                pages.add(Text(text))
                            }
                        }
                    } catch (_: Exception) {
                        // Silently ignore content read errors
                    }
                }
            }
        } catch (_: Exception) {
            // Silently ignore EPUB read errors
        }
        
        return pages
    }
    
    private fun readTextFile(file: File): List<Page> {
        return try {
            val content = file.readText()
            content.split(Regex("\n\n+|\n"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { Text(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }
    
    private fun findOpfFile(zip: ZipFile): java.util.zip.ZipEntry? {
        val containerEntry = zip.getEntry("META-INF/container.xml")
        if (containerEntry != null) {
            val containerXml = zip.getInputStream(containerEntry).bufferedReader().readText()
            val doc = Ksoup.parse(containerXml, Parser.xmlParser())
            val rootfile = doc.selectFirst("rootfile[full-path]")
            val opfPath = rootfile?.attr("full-path")
            if (opfPath != null) {
                return zip.getEntry(opfPath)
            }
        }
        
        return zip.entries().asSequence().find { it.name.endsWith(".opf") }
    }
    
    // CatalogSource implementation
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        val novels = scanLocalNovels()
        return MangasPageInfo(novels, hasNextPage = false)
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return getMangaList(null, page)
    }
    
    override fun getListings(): List<Listing> {
        return listOf(
            object : Listing("All") {}
        )
    }
    
    override fun getFilters(): FilterList {
        return emptyList()
    }
    
    override fun getCommands(): CommandList {
        return emptyList()
    }
    
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return manga
    }
    
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return scanNovelChapters(manga.key)
    }
    
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return readChapterFile(chapter.key)
    }
    
    override fun getLocalFolderPath(): String {
        return getLocalDirectory().absolutePath
    }
    
    companion object {
        private val supportedFormats = setOf("epub", "txt")
    }
}
