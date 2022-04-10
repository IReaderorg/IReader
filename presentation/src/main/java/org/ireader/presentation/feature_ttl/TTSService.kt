package org.ireader.presentation.feature_ttl

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
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
        const val NEXT_PAR = 4
        const val SKIP_NEXT = 5
        const val PLAY = 6
        const val PAUSE = 7
        const val CANCEL = 8
        lateinit var ttsWork: OneTimeWorkRequest
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
            }


            startService(command)



            return Result.success()
        } catch (e: Exception) {
            return Result.failure()

        }

    }

    suspend fun updateNotification(
        chapter: Chapter,
        book: Book,
        error: Boolean = false,
        isLoading: Boolean? = null,
    ) {
        NotificationManagerCompat.from(context).apply {
            val builder =
                defaultNotificationHelper.basicPlayingTextReaderNotification(
                    chapter,
                    book,
                    state.isPlaying,
                    state.currentReadingParagraph,
                    state.mediaSession,
                    isLoading = isLoading ?: state.ttsIsLoading,
                    isError = error
                )

            notify(Notifications.ID_TEXT_READER_PROGRESS,
                builder.build())
        }
    }

    suspend fun startService(command: Int) {
        try {
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
                                    state.mediaSession,
                                    isLoading = state.ttsIsLoading
                                )
                            NotificationManagerCompat.from(context)
                                .notify(Notifications.ID_TEXT_READER_PROGRESS,
                                    notification.build())


                            when (command) {
                                CANCEL -> {
                                    NotificationManagerCompat.from(context)
                                        .cancel(Notifications.ID_TEXT_READER_PROGRESS)
                                }
                                SKIP_PREV -> {
                                    state.tts.stop()
                                    updateNotification(chapter, book)
                                    val index = getChapterIndex(chapter, chapters)
                                    if (index > 0) {
                                        val id = chapters[index - 1].id
                                        getRemoteChapter(chapterId = id, source, state)
                                    }
                                    state.currentReadingParagraph = 0
                                    updateNotification(chapter, book)
                                    if (state.isPlaying) {
                                        readText(context, state.mediaSession)
                                    } else {

                                    }
                                }
                                PREV_PAR -> {
                                    state.ttsContent?.value?.let { content ->
                                        if (state.currentReadingParagraph > 0 && state.currentReadingParagraph in 0..content.lastIndex) {
                                            state.currentReadingParagraph -= 1
                                            updateNotification(chapter, book)
                                            if (state.isPlaying) {
                                                readText(context, state.mediaSession)
                                            }
                                        }
                                    }
                                }
                                SKIP_NEXT -> {
                                    state.tts.stop()
                                    updateNotification(chapter, book)
                                    val index = getChapterIndex(chapter, chapters)
                                    if (index != chapters.lastIndex) {
                                        val id = chapters[index + 1].id
                                        getRemoteChapter(chapterId = id, source, state)
                                    }
                                    state.currentReadingParagraph = 0
                                    updateNotification(chapter, book)
                                    if (state.isPlaying) {
                                        readText(context, state.mediaSession)
                                    } else {
                                    }


                                }
                                NEXT_PAR -> {
                                    state.ttsContent?.value?.let { content ->
                                        if (state.currentReadingParagraph > 0 && state.currentReadingParagraph < content.size) {
                                            if (state.currentReadingParagraph < content.lastIndex) {
                                                state.currentReadingParagraph += 1
                                            }
                                            if (state.isPlaying) {
                                                readText(context, state.mediaSession)
                                                updateNotification(chapter, book)
                                            }
                                        }
                                    }


                                }
                                PLAY -> {
                                    state.isPlaying = true
                                    readText(context, state.mediaSession)
                                }
                                PAUSE -> {
                                    state.isPlaying = false
                                    state.tts.stop()
                                    updateNotification(chapter, book)
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
        } catch (e: java.lang.Exception) {
            Timber.e(e)
            state.ttsChapter?.let { chapter ->
                state.ttsSource?.let { source ->
                    state.ttsChapters.let { chapters ->
                        state.ttsBook?.let { book ->
                            updateNotification(chapter, book, error = true)
                        }
                    }
                }
            }

        }
    }

    val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    fun readText(context: Context, mediaSessionCompat: MediaSessionCompat) {


        state.apply {
            if (state.languages.isEmpty()) {
                tts.availableLanguages?.let {
                    state.languages = it.toList()
                }
            }
            if (state.voices.isEmpty()) {
                tts.voices?.toList()?.let {
                    state.voices = it
                }
            }
            if (currentVoice != prevVoice) {
                prevVoice = currentVoice
                tts.voices?.firstOrNull { it.name == currentVoice }?.let {
                    tts.voice = it
                }
            }

            if (currentLanguage != prevLanguage) {
                prevLanguage = currentLanguage
                tts.availableLanguages?.firstOrNull { it.displayName == currentLanguage }
                    ?.let {
                        tts.language = it
                    }
            }

            if (pitch != prevPitch) {
                prevPitch = pitch
                tts.setPitch(pitch)
            }
            if (speechSpeed != prevSpeechSpeed) {
                prevSpeechSpeed = speechSpeed
                tts.setSpeechRate(speechSpeed)
            }

            ttsChapter?.let { chapter ->
                ttsContent?.value?.let { content ->
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
                                            mediaSessionCompat,
                                            isLoading = state.ttsIsLoading)

                                    notify(Notifications.ID_TEXT_READER_PROGRESS,
                                        builder.build())
                                }
                                if (state.utteranceId != (currentReadingParagraph).toString()) {
                                    tts.speak(content[currentReadingParagraph],
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        currentReadingParagraph.toString())
                                }



                                tts.setOnUtteranceProgressListener(object :
                                    UtteranceProgressListener() {
                                    override fun onStop(
                                        utteranceId: String?,
                                        interrupted: Boolean,
                                    ) {
                                        super.onStop(utteranceId, interrupted)
                                        state.utteranceId = ""

                                    }

                                    override fun onStart(p0: String) {
                                        // showTextReaderNotification(context)
                                        isPlaying = true
                                        utteranceId = p0

                                    }

                                    override fun onDone(p0: String) {
                                        try {
                                            var isFinished = false



                                            if (currentReadingParagraph < content.size) {
                                                if (currentReadingParagraph < content.lastIndex) {
                                                    currentReadingParagraph += 1
                                                } else {
                                                    isFinished = true
                                                }

                                                readText(context, mediaSessionCompat)


//                                                if (voiceMode) {
//
//                                                    val voicemode =
//                                                        p0 == currentReadingParagraph.toString()
//                                                    if (voicemode) {
//                                                        isPlaying = true
//                                                        readText(context, mediaSessionCompat)
//                                                    }
//                                                } else {
//
//                                                }

                                            }


                                            if (currentReadingParagraph == content.lastIndex && !ttsIsLoading && isFinished) {
                                                isPlaying = false

                                                tts.stop()
                                                if (autoNextChapter) {
                                                    scope.launch {
                                                        state.ttsChapters.let { chapters ->
                                                            state.ttsSource?.let { source ->
                                                                val index =
                                                                    getChapterIndex(chapter,
                                                                        chapters)
                                                                if (index != chapters.lastIndex) {
                                                                    val id = chapters[index + 1].id
                                                                    getRemoteChapter(chapterId = id,
                                                                        source,
                                                                        state)
                                                                }
                                                                state.currentReadingParagraph = 0
                                                                updateNotification(chapter, book)
                                                                if (state.isPlaying) {
                                                                    readText(context,
                                                                        state.mediaSession)
                                                                }
                                                            }
                                                        }

                                                    }
                                                }

                                            }

                                        } catch (e: Exception) {
                                            Timber.e(e)
                                        }
                                    }

                                    override fun onError(p0: String) {
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
    }

    suspend fun getRemoteChapter(
        chapterId: Long,
        source: Source,
        ttsState: TTSState,
    ) {
        try {


            ttsState.ttsChapter?.let { chapter ->
                ttsState.ttsBook?.let { book ->
                    updateNotification(chapter = chapter, book = book, isLoading = true)
                }
            }

            ttsState.ttsIsLoading = true
            state.currentReadingParagraph = 0
            val localChapter = chapterRepo.findChapterById(chapterId)

            if (localChapter?.isChapterNotEmpty() == true) {
                state.ttsChapter = localChapter
                state.currentReadingParagraph = 0
                ttsState.ttsIsLoading = false
                ttsState.ttsBook?.let { book ->
                    updateNotification(chapter = localChapter, book = book)

                }
                insertUseCases.insertChapter(localChapter.copy(
                    read = true,
                    readAt = Clock.System.now()
                        .toEpochMilliseconds(),
                ))
            } else {
                if (localChapter != null) {
                    remoteUseCases.getRemoteReadingContent(
                        chapter = localChapter,
                        source,
                        onSuccess = { result ->
                            if (result.content.joinToString().length > 1) {
                                state.ttsChapter = result
                                insertUseCases.insertChapter(result.copy(
                                    dateFetch = Clock.System.now()
                                        .toEpochMilliseconds(),
                                    read = true,
                                    readAt = Clock.System.now()
                                        .toEpochMilliseconds(),
                                ))
                                chapterRepo.findChaptersByBookId(result.bookId).let { res ->
                                    state.ttsChapters = res
                                }
                                state.currentReadingParagraph = 0
                                ttsState.ttsIsLoading = false
                                ttsState.ttsBook?.let { book ->
                                    updateNotification(chapter = localChapter, book = book)
                                }
                            }
                        }, onError = {
                            ttsState.ttsIsLoading = false

                        }
                    )
                }
            }
            ttsState.ttsIsLoading = false
        } catch (e: java.lang.Exception) {
            Timber.e(e)
            ttsState.ttsChapter?.let { chapter ->
                ttsState.ttsBook?.let { book ->
                    updateNotification(chapter = chapter,
                        book = book,
                        isLoading = false,
                        error = true)
                }
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
