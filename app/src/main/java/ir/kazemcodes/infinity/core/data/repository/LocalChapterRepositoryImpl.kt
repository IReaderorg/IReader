package ir.kazemcodes.infinity.core.data.repository

import ir.kazemcodes.infinity.core.data.local.dao.LibraryChapterDao
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class LocalChapterRepositoryImpl @Inject constructor(private val daoLibrary: LibraryChapterDao) :
    LocalChapterRepository {

    override suspend fun insertChapters(
        chapters: List<Chapter>,
        book: Book,
        inLibrary: Boolean,
        source: Source,
    ) {
        return daoLibrary.insertChapters(chapters = chapters.map {
            it.copy(bookName = book.bookName,
                source = source.name,
                inLibrary = inLibrary)
        })
    }


    override suspend fun deleteLastReadChapter(
        bookName: String,
        source: String,
    ) {
        return daoLibrary.deleteLastReadChapter(bookName, source)
    }

    override suspend fun setLastReadChapter(
        bookName: String,
        chapterTitle: String,
        source: String,
    ) {
        return daoLibrary.setLastReadChapter(bookName = bookName, chapterTitle, source)
    }

    override fun getLastReadChapter(bookName: String, source: String): Flow<Resource<Chapter>> =
        flow {
            try {
                Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Called")
                emit(Resource.Loading())
                daoLibrary.getLastReadChapter(bookName, source).collect { chapter ->
                    if (chapter != null) {
                        emit(Resource.Success(data = chapter))
                    } else {
                        emit(Resource.Error<Chapter>(message = Constants.NO_CHAPTER_ERROR))
                    }
                }
                Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Finished Successfully")
            } catch (e: Exception) {
                emit(Resource.Error<Chapter>(message = e.localizedMessage
                    ?: Constants.NO_CHAPTER_ERROR))
            }
        }


    override suspend fun updateChapter(
        readingContent: String,
        haveBeenRead: Boolean,
        bookName: String,
        chapterTitle: String,
        lastRead: Boolean,
        source: String,
    ) {
        return daoLibrary.updateChapter(readingContent = readingContent,
            bookName = bookName,
            chapterTitle = chapterTitle,
            haveBeenRead = haveBeenRead,
            lastRead = lastRead, source)
    }

    override suspend fun updateChapter(chapter: Chapter) {
        return daoLibrary.updateChapter(chapter)
    }

    override suspend fun updateChapters(chapters: List<Chapter>) {
        return daoLibrary.updateChapters(chapters)
    }

    override suspend fun updateAddToLibraryChapters(
        chapterTitle: String,
        source: String,
        bookName: String,
    ) {
        return daoLibrary.updateAddToLibraryChapters(chapterTitle, source, bookName)
    }

    override fun getChapterByName(bookName: String, source: String): Flow<Resource<List<Chapter>>> =
        flow {
            emit(Resource.Loading())
            try {
                Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Called")
                emit(Resource.Loading())
                daoLibrary.getChapters(bookName, source).collect { chapters ->
                    if (chapters != null) {
                        try {
                            emit(Resource.Success<List<Chapter>>(data = chapters.sortedWith(object :
                                Comparator<Chapter> {
                                override fun compare(o1: Chapter, o2: Chapter): Int {
                                    return extractInt(o1) - extractInt(o2)
                                }

                                fun extractInt(s: Chapter): Int {
                                    val num = s.title.replace("\\D".toRegex(), "")
                                    // return 0 if no digits found
                                    return if (num.isEmpty()) 0 else Integer.parseInt(num)
                                }
                            })))
                        } catch (e: NumberFormatException) {
                            emit(Resource.Success<List<Chapter>>(data = chapters))
                        }


                    } else {
                        emit(Resource.Error<List<Chapter>>(message = Constants.NO_CHAPTER_ERROR))
                    }

                }
                Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Finished Successfully")
            } catch (e: Exception) {
                emit(Resource.Error<List<Chapter>>(message = e.localizedMessage
                    ?: Constants.NO_CHAPTER_ERROR))
            }
        }


    override fun getAllChapter(): Flow<Resource<List<Chapter>>> = flow {
        try {
            Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Called")
            emit(Resource.Loading())
            daoLibrary.getAllChapters()
                .collect { chapters ->
                    if (!chapters.isNullOrEmpty()) {
                        emit(Resource.Success<List<Chapter>>(data = chapters))
                    } else {
                        emit(Resource.Error<List<Chapter>>(message = Constants.NO_CHAPTERS_ERROR))
                    }

                }
            Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Finished Successfully")
        } catch (e: Exception) {
            emit(Resource.Error<List<Chapter>>(message = e.localizedMessage
                ?: Constants.NO_CHAPTERS_ERROR))
        }
    }


    override fun getChapterByChapter(
        chapterTitle: String,
        bookName: String,
        source: String,
    ): Flow<Resource<Chapter>> = flow {
        try {
            Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Called")
            emit(Resource.Loading())
            daoLibrary.getChapterByChapter(chapterTitle, bookName, source).first { chapter ->
                if (chapter != null) {
                    emit(Resource.Success(data = chapter))
                    return@first true
                } else {
                    emit(Resource.Error(message = Constants.NO_CHAPTERS_ERROR))
                    return@first false
                }

            }
            Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Finished Successfully")
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage
                ?: Constants.NO_CHAPTERS_ERROR))
        }
    }


    override suspend fun deleteChapters(bookName: String, source: String) {
        return daoLibrary.deleteLocalChaptersByName(bookName = bookName, source)
    }

    override suspend fun deleteNotInLibraryChapters() {
        return daoLibrary.deleteLibraryChapters()
    }

    override suspend fun deleteAllChapters() {
        return daoLibrary.deleteAllChapters()
    }
}