package org.ireader.presentation.feature_ttl

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.presentation.feature_services.notification.DefaultNotificationHelper
import org.ireader.presentation.feature_services.notification.Notifications
import tachiyomi.source.Source
import timber.log.Timber

@HiltWorker
class TTSService @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val bookRepo: LocalBookRepository,
    private val chapterRepo: LocalChapterRepository,
    private val remoteUseCases: RemoteUseCases,
    private val extensions: CatalogStore,
    private val defaultNotificationHelper: DefaultNotificationHelper,
    private val state: TTSStateImpl,
    private val insertUseCases: LocalInsertUseCases,
) : CoroutineWorker(context, params) {
    companion object {
        const val TTS_SERVICE_NAME = "TTS_SERVICE"
        const val TTS_Chapter_ID = "chapterId"
        const val COMMAND = "command"
        const val TTS_BOOK_ID = "bookId"

        const val SKIP_PREV = 1
        const val PREV_PAR = 2
        const val PLAY_PAUSE = 3
        const val SKIP_NEXT = 5
        const val NEXT_PAR = 4
    }


    override suspend fun doWork(): Result {

        try {


            val chapterId = inputData.getLong(TTS_Chapter_ID, -1)
            val booksId = inputData.getLong(TTS_BOOK_ID, -1)
            val command = inputData.getInt(COMMAND, -1)


            if (chapterId != -1L && booksId != -1L) {
                val book = bookRepo.findBookById(booksId)
                val chapter = chapterRepo.findChapterById(chapterId)
                val chapters = chapterRepo.findChaptersByBookId(booksId)
                val source = book?.sourceId?.let { extensions.get(it)?.source }
                if (chapter != null && source != null) {
                    state.ttsBook = book
                    state.ttsChapter = chapter
                    state.ttsChapters = chapters
                    state.ttsSource = source
                }
            } else {

            }



            state.ttsChapter?.let { chapter ->
                state.ttsSource?.let { source ->
                    state.ttsChapters.let { chapters ->
                        state.ttsBook?.let { book ->
                            val notification =
                                defaultNotificationHelper.basicPlayingTextReaderNotification(
                                    chapter,
                                    book,
                                    state.isPlaying,
                                    state.currentReadingParagraph,
                                    state.mediaSession)
                            NotificationManagerCompat.from(context)
                                .notify(Notifications.ID_TEXT_READER_PROGRESS, notification.build())


                            when (command) {
                                SKIP_PREV -> {
                                    val index = getChapterIndex(chapter, chapters)
                                    if (index > 0) {
                                        val id = chapters[index - 1].id
                                        getRemoteChapter(chapterId = id, source)
                                    }
                                    state.currentReadingParagraph = 0
                                    updateNotification(chapter, book)
                                    if (state.isPlaying) {
                                        readText(context, state.mediaSession)
                                    } else {

                                    }
                                }
                                PREV_PAR -> {
                                    state.currentReadingParagraph -= 1
                                    updateNotification(chapter, book)
                                    if (state.isPlaying) {
                                        readText(context, state.mediaSession)
                                    } else {

                                    }
                                }
                                SKIP_NEXT -> {
                                    val index = getChapterIndex(chapter, chapters)
                                    if (index != chapters.lastIndex) {
                                        val id = chapters[index + 1].id
                                        getRemoteChapter(chapterId = id, source)
                                    }
                                    state.currentReadingParagraph = 0
                                    updateNotification(chapter, book)
                                    if (state.isPlaying) {
                                        readText(context, state.mediaSession)
                                    } else {

                                    }
                                }
                                NEXT_PAR -> {
                                    state.currentReadingParagraph += 1
                                    if (state.isPlaying) {
                                        readText(context, state.mediaSession)
                                        updateNotification(chapter, book)
                                    } else {

                                    }
                                }
                                else -> {
                                    if (state.isPlaying) {
                                        //readText(context, state.mediaSession)
                                        state.isPlaying = false
                                        state.tts.stop()
                                        updateNotification(chapter, book)
                                    } else {
                                        state.isPlaying = true
                                        readText(context, state.mediaSession)
                                    }
                                }
                            }
                        }
                    }
                }

            }



            return Result.success()
        } catch (e: Exception) {
            return Result.failure()

        }

    }

    suspend fun updateNotification(chapter: Chapter, book: Book) {
        NotificationManagerCompat.from(context).apply {
            val builder =
                defaultNotificationHelper.basicPlayingTextReaderNotification(
                    chapter,
                    book,
                    state.isPlaying,
                    state.currentReadingParagraph,
                    state.mediaSession)

            notify(Notifications.ID_TEXT_READER_PROGRESS,
                builder.build())
        }
    }

    val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    fun readText(context: Context, mediaSessionCompat: MediaSessionCompat) {


        state.apply {
            tts.availableLanguages?.let {
                state.languages = it.toList()
            }
            tts.voices?.toList()?.let {
                state.voices = it
            }
            tts.voices?.firstOrNull { it.name == currentVoice }?.let {
                tts.voice = it
            }
            tts.availableLanguages?.firstOrNull { it.displayName == currentLanguage }
                ?.let {
                    tts.language = it
                }

            tts.setPitch(pitch)
            tts.setSpeechRate(speechSpeed)

            ttsChapter?.let { chapter ->
                state.ttsBook?.let { book ->
                    try {

                        NotificationManagerCompat.from(context).apply {
                            scope.launch {
                                val builder =
                                    defaultNotificationHelper.basicPlayingTextReaderNotification(
                                        chapter,
                                        book,
                                        isPlaying,
                                        currentReadingParagraph,
                                        mediaSessionCompat)

                                notify(Notifications.ID_TEXT_READER_PROGRESS, builder.build())
                            }






                            tts.speak(chapter.content[currentReadingParagraph],
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                currentReadingParagraph.toString())

                            tts.setOnUtteranceProgressListener(object :
                                UtteranceProgressListener() {
                                override fun onStop(
                                    utteranceId: String?,
                                    interrupted: Boolean,
                                ) {
                                    super.onStop(utteranceId, interrupted)

                                    isPlaying = false
                                }

                                override fun onStart(p0: String?) {
                                    // showTextReaderNotification(context)
                                    isPlaying = true
                                }

                                override fun onDone(p0: String?) {
                                    if (currentReadingParagraph < chapter.content.size && isPlaying) {
                                        currentReadingParagraph += 1
                                        //  builder.setProgress(chapter.content.size, currentReadingParagraph,false)
                                        //  notify(Notifications.ID_TEXT_READER_PROGRESS, builder.build())
                                        readText(context, mediaSessionCompat)
                                    }
                                    if (currentReadingParagraph == chapter.content.size && !ttsIsLoading) {
                                        isPlaying = false
                                        tts.stop()

                                    }
                                }

                                override fun onError(p0: String?) {
                                    isPlaying = false
                                }


                                override fun onBeginSynthesis(
                                    utteranceId: String?,
                                    sampleRateInHz: Int,
                                    audioFormat: Int,
                                    channelCount: Int,
                                ) {
                                    super.onBeginSynthesis(utteranceId,
                                        sampleRateInHz,
                                        audioFormat,
                                        channelCount)
                                }
                            })
                        }
                    } catch (e: Exception) {
                        Timber.e(e.localizedMessage)
                    }

                }
            }
        }
    }

    suspend fun getRemoteChapter(
        chapterId: Long,
        source: Source,
    ) {
        val localChapter = chapterRepo.findChapterById(chapterId)
        if (localChapter?.isChapterNotEmpty() == true) {
            state.ttsChapter = localChapter
        } else {
            if (localChapter != null) {
                remoteUseCases.getRemoteReadingContent(
                    chapter = localChapter,
                    source,
                    onSuccess = { result ->
                        if (result.content.joinToString().length > 1) {
                            state.ttsChapter = result
                            insertUseCases.insertChapter(result)
                            chapterRepo.findChaptersByBookId(result.bookId).let { res ->
                                state.ttsChapters = res
                            }
                        }
                    }, onError = {
                        throw Exception("No result")
                    }
                )
            }
        }

    }

    fun getChapterIndex(chapter: Chapter, chapters: List<Chapter>): Int {
        val chaptersIds = chapters.map { it.title }
        val index = chaptersIds.indexOfFirst { it == chapter.title }

        return if (index != -1) {
            index
        } else {
            throw Exception("Invalid Id")
        }
    }


}
