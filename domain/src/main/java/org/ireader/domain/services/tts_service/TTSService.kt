package org.ireader.domain.services.tts_service

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
import kotlinx.datetime.Clock
import org.ireader.core_api.source.Source
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.notification.Notifications
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.services.downloaderService.DefaultNotificationHelper
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.presentation.feature_ttl.TTSState
import org.ireader.presentation.feature_ttl.TTSStateImpl
import timber.log.Timber

@HiltWorker
class TTSService @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val bookRepo: LocalBookRepository,
    private val chapterRepo: LocalChapterRepository,
    private val remoteUseCases: RemoteUseCases,
    private val extensions: CatalogStore,
    private val notificationHelper: DefaultNotificationHelper,
    private val state: TTSStateImpl,
    private val insertUseCases: LocalInsertUseCases,
) : CoroutineWorker(context, params) {
    companion object {
        const val TTS_SERVICE_NAME = "TTS_SERVICE"
        const val TTS_Chapter_ID = "chapterId"
        const val COMMAND = "command"
        const val TTS_BOOK_ID = "bookId"

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
        } catch (e: Throwable) {
            return Result.failure()

        }

    }

    suspend fun updateNotification(
        chapter: Chapter,
        book: Book,
        error: Boolean = false,
        isLoading: Boolean? = null,
    ) {
        state.mediaSession?.let { mediaSession ->
            NotificationManagerCompat.from(context).apply {
                val builder =
                    notificationHelper.basicPlayingTextReaderNotification(
                        chapter,
                        book,
                        state.isPlaying,
                        state.currentReadingParagraph,
                        mediaSession,
                        isLoading = isLoading ?: state.ttsIsLoading,
                        isError = error
                    )

                notify(Notifications.ID_TEXT_READER_PROGRESS,
                    builder.build())
            }
        }
    }

    suspend fun startService(command: Int) {
        try {
            if (state.player == null) {
                state.player = TextToSpeech(context) { status ->
                    state.ttsIsLoading = true
                    if (status == TextToSpeech.ERROR) {
                        Timber.e("Text-to-Speech Not Available")
                        state.ttsIsLoading = false
                        return@TextToSpeech
                    }
                    state.ttsIsLoading = false
                }
            }
            if (state.mediaSession == null) {
                state.mediaSession = MediaSessionCompat(context, "mediaPlayer", null, null)
            }



            state.player?.let { tts ->
                state.mediaSession?.let { mediaSession ->
                    state.ttsChapter?.let { chapter ->
                        state.ttsSource?.let { source ->
                            state.ttsChapters.let { chapters ->
                                state.ttsBook?.let { book ->
                                    val notification =
                                        notificationHelper.basicPlayingTextReaderNotification(
                                            chapter,
                                            book,
                                            state.isPlaying,
                                            state.currentReadingParagraph,
                                            mediaSession,
                                            isLoading = state.ttsIsLoading
                                        ).build()

                                    NotificationManagerCompat.from(context)
                                        .notify(Notifications.ID_TEXT_READER_PROGRESS,
                                            notification)


                                    when (command) {
                                        Player.CANCEL -> {
                                            NotificationManagerCompat.from(context)
                                                .cancel(Notifications.ID_TEXT_READER_PROGRESS)
                                        }
                                        Player.SKIP_PREV -> {
                                            tts.stop()
                                            updateNotification(chapter, book)
                                            val index = getChapterIndex(chapter, chapters)
                                            if (index > 0) {
                                                val id = chapters[index - 1].id
                                                getRemoteChapter(chapterId = id, source, state) {
                                                    if (state.isPlaying) {
                                                        readText(context, mediaSession)
                                                    }
                                                }
                                            }
                                            state.currentReadingParagraph = 0
                                            updateNotification(chapter, book)
                                            if (state.isPlaying) {
                                                readText(context, mediaSession)
                                            } else {

                                            }
                                        }
                                        Player.PREV_PAR -> {
                                            state.ttsContent?.value?.let { content ->
                                                if (state.currentReadingParagraph > 0 && state.currentReadingParagraph in 0..content.lastIndex) {
                                                    state.currentReadingParagraph -= 1
                                                    updateNotification(chapter, book)
                                                    if (state.isPlaying) {
                                                        readText(context, mediaSession)
                                                    }
                                                }
                                            }
                                        }
                                        Player.SKIP_NEXT -> {
                                            tts.stop()
                                            updateNotification(chapter, book)
                                            val index = getChapterIndex(chapter, chapters)
                                            if (index != chapters.lastIndex) {
                                                val id = chapters[index + 1].id
                                                getRemoteChapter(chapterId = id, source, state) {
                                                    if (state.isPlaying) {
                                                        readText(context, mediaSession)
                                                    }
                                                }
                                            }
                                            state.currentReadingParagraph = 0
                                            updateNotification(chapter, book)
                                            if (state.isPlaying) {
                                                readText(context, mediaSession)
                                            } else {
                                            }


                                        }
                                        Player.NEXT_PAR -> {
                                            state.ttsContent?.value?.let { content ->
                                                if (state.currentReadingParagraph >= 0 && state.currentReadingParagraph < content.size) {
                                                    if (state.currentReadingParagraph < content.lastIndex) {
                                                        state.currentReadingParagraph += 1
                                                    }
                                                    if (state.isPlaying) {
                                                        readText(context, mediaSession)
                                                        updateNotification(chapter, book)
                                                    }
                                                }
                                            }


                                        }
                                        Player.PLAY -> {
                                            state.isPlaying = true
                                            readText(context, mediaSession)
                                        }
                                        Player.PAUSE -> {
                                            state.isPlaying = false
                                            tts.stop()
                                            updateNotification(chapter, book)
                                        }
                                        Player.PLAY_PAUSE -> {
                                            if (state.isPlaying) {
                                                state.isPlaying = false
                                                tts.stop()
                                                updateNotification(chapter, book)
                                            } else {
                                                state.isPlaying = true
                                                readText(context, mediaSession)
                                            }
                                        }
                                        else -> {
                                            if (state.isPlaying) {
                                                state.isPlaying = false
                                                tts.stop()
                                                updateNotification(chapter, book)
                                            } else {
                                                state.isPlaying = true
                                                readText(context, mediaSession)
                                            }
                                        }
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
                player?.availableLanguages?.let {
                    state.languages = it.toList()
                }
            }
            if (state.voices.isEmpty()) {
                player?.voices?.toList()?.let {
                    state.voices = it
                }
            }
            if (currentVoice != prevVoice) {
                prevVoice = currentVoice
                player?.voices?.firstOrNull { it.name == currentVoice }?.let {
                    player?.voice = it
                }
            }

            if (currentLanguage != prevLanguage) {
                prevLanguage = currentLanguage
                player?.availableLanguages?.firstOrNull { it.displayName == currentLanguage }
                    ?.let {
                        player?.language = it
                    }
            }

            if (pitch != prevPitch) {
                prevPitch = pitch
                player?.setPitch(pitch)
            }
            if (speechSpeed != prevSpeechSpeed) {
                prevSpeechSpeed = speechSpeed
                player?.setSpeechRate(speechSpeed)
            }

            ttsChapter?.let { chapter ->
                ttsContent?.value?.let { content ->
                    state.ttsBook?.let { book ->
                        try {

                            NotificationManagerCompat.from(context).apply {
                                scope.launch {
                                    val builder =
                                        notificationHelper.basicPlayingTextReaderNotification(
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
                                    player?.speak(content[currentReadingParagraph],
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        currentReadingParagraph.toString())
                                }



                                player?.setOnUtteranceProgressListener(object :
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
                                            }


                                            if (currentReadingParagraph == content.lastIndex && !ttsIsLoading && isFinished) {
                                                isPlaying = false
                                                currentReadingParagraph = 0

                                                player?.stop()
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
                                                                        state) {
                                                                        state.currentReadingParagraph =
                                                                            0
                                                                        updateNotification(chapter,
                                                                            book)

                                                                        state.mediaSession?.let { mediaSession ->
                                                                            readText(context,
                                                                                mediaSession)
                                                                        }


                                                                    }
                                                                }

                                                            }
                                                        }

                                                    }
                                                }

                                            }

                                        } catch (e: Throwable) {
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
                        } catch (e: Throwable) {
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
        onSuccess: suspend () -> Unit,
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
                onSuccess()
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
                                onSuccess()
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
