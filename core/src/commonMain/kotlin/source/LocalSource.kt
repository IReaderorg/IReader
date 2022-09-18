package ireader.core.source

import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.Page

class LocalSource : Source {
    override val id: Long
        get() = -200
    override val name: String
        get() = "Local Source"
    override val lang: String
        get() = "en"

    companion object {
        const val SOURCE_ID = -200L
    }
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        throw LocalSourceException()
    }

    override suspend fun getChapterList(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): List<ChapterInfo> {
        throw LocalSourceException()
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        throw LocalSourceException()
    }
}

class LocalSourceException : Exception("this is a local source")

class CorruptedSource(sourceId: Long) : Source {
    override val id: Long
        get() = -201
    override val name: String
        get() = "Local Source"
    override val lang: String
        get() = "en"

    companion object {
        const val SOURCE_ID = -200L
    }
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        throw LocalSourceException()
    }

    override suspend fun getChapterList(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): List<ChapterInfo> {
        throw LocalSourceException()
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        throw LocalSourceException()
    }
}

