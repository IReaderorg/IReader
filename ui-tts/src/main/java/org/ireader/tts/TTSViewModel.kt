package org.ireader.tts

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ireader.common_extensions.findComponentActivity
import org.ireader.common_models.entities.Chapter
import org.ireader.core_api.log.Log
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.services.tts_service.Player
import org.ireader.domain.services.tts_service.TTSState
import org.ireader.domain.services.tts_service.TTSStateImpl
import org.ireader.domain.services.tts_service.media_player.TTSService
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.TextReaderPrefUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.use_cases.services.ServiceUseCases
import javax.inject.Inject

@HiltViewModel
class TTSViewModel @Inject constructor(
    val ttsState: TTSStateImpl,
    private val savedStateHandle: SavedStateHandle,
    private val serviceUseCases: ServiceUseCases,
    private val getBookUseCases: LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    val speechPrefUseCases: TextReaderPrefUseCase,
    private val readerUseCases: ReaderPrefUseCases
) : BaseViewModel(),
    TTSState by ttsState {

    private var chapterId: Long = -1
    private var initialize: Boolean = false

    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)

        kotlin.runCatching {
            val readingParagraph =
                savedStateHandle.get<String>(NavigationArgs.readingParagraph.name)
            if (readingParagraph != null && readingParagraph.toInt() < (ttsState.ttsContent?.value?.lastIndex
                    ?: 0)
            ) {
                ttsState.currentReadingParagraph = readingParagraph.toInt()
            }

        }

        if (sourceId != null && chapterId != null && bookId != null) {
            this.chapterId = chapterId
            ttsCatalog = getLocalCatalog.get(sourceId = sourceId)
            viewModelScope.launch {
                val book = getBookUseCases.findBookById(bookId)
                ttsBook = book
            }

            getLocalChapter(chapterId)
            subscribeChapters(bookId)
            readPreferences()
            if (ttsChapter?.id != chapterId) {
                runTTSService(Player.PAUSE)
            }
            initialize = true
        }
    }

    fun readPreferences() {
        viewModelScope.launch {
            speechSpeed = speechPrefUseCases.readRate()
            pitch = speechPrefUseCases.readPitch()
            currentLanguage = speechPrefUseCases.readLanguage()
            autoNextChapter = speechPrefUseCases.readAutoNext()
            font = readerUseCases.selectedFontStateUseCase.readFont()
            lineHeight = readerUseCases.fontHeightUseCase.read()
            currentVoice = speechPrefUseCases.readVoice()
        }
    }

    var controller: MediaControllerCompat? = null
    private var ctrlCallback: TTSController? = null
    private var textReader: TextToSpeech? = null
    fun initMedia(context: Context) {
        if (browser == null) {
            browser = MediaBrowserCompat(
                context,
                ComponentName(context, TTSService::class.java),
                connCallback(context),
                null
            )
        }
        if (!isServiceConnected) {
            browser?.connect()
        }

        if (textReader == null) {
            textReader = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsState.languages = textReader?.availableLanguages?.toList() ?: emptyList()
                    ttsState.voices = textReader?.voices?.toList() ?: emptyList()
                }

            }
        }
    }

    override fun onDestroy() {
        browser?.disconnect()
        textReader?.shutdown()
        super.onDestroy()
    }

    var browser: MediaBrowserCompat? = null
    private fun connCallback(context: Context) = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            isServiceConnected = true
            browser?.sessionToken?.also { token ->
                controller = MediaControllerCompat(context, token)
                context.findComponentActivity()?.let { activity ->
                    MediaControllerCompat.setMediaController(
                        activity,
                        controller
                    )
                }
            }
            initController()
        }

        override fun onConnectionSuspended() {
            Log.debug { "TTS ViewModel: MediaSession: onConnectionSuspended" }
            isServiceConnected = false
            ctrlCallback?.let { controller?.unregisterCallback(it) }
            controller = null
            browser = null
            textReader = null
        }

        override fun onConnectionFailed() {
            isServiceConnected = false
            Log.debug { "TTS ViewModel: MediaSession:  onConnectionFailed" }
            super.onConnectionFailed()
        }
    }

    fun initController() {
        ctrlCallback = TTSController()
        ctrlCallback?.let { ctrlCallback ->
            controller?.registerCallback(ctrlCallback)
            ctrlCallback.onMetadataChanged(controller?.metadata)
            ctrlCallback.onPlaybackStateChanged(controller?.playbackState)
        }
    }

    private inner class TTSController : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            meta = metadata
            if (metadata == null || !initialize) return
            val novelId = metadata.getLong(TTSService.NOVEL_ID)
            val currentParagraph = metadata.getLong(TTSService.PROGRESS)
            val chapterId = metadata.getLong(TTSService.CHAPTER_ID)
            val isLoading = metadata.getLong(TTSService.IS_LOADING)
            val error = metadata.getLong(TTSService.ERROR)
            if (ttsBook?.id != novelId && novelId != -1L) {
                viewModelScope.launch {
                    ttsBook = getBookUseCases.findBookById(novelId)
                }
            }
            if (currentParagraph != currentReadingParagraph.toLong() && currentParagraph != -1L) {
                currentReadingParagraph = currentParagraph.toInt()
            }
            if (chapterId != ttsChapter?.id && chapterId != -1L) {
                viewModelScope.launch {
                    ttsChapter = getChapterUseCase.findChapterById(chapterId)
                }
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            if (state == null) return
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    isPlaying = true
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    isPlaying = false
                }
                PlaybackStateCompat.STATE_NONE -> {
                    isPlaying = false
                }
                PlaybackStateCompat.STATE_BUFFERING -> {
                    isPlaying = false
                }
                else -> {}
            }
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            if (extras == null) return
        }
    }

    fun runTTSService(command: Int = -1) {
        serviceUseCases.startTTSServicesUseCase(
            chapterId = ttsChapter?.id,
            bookId = ttsBook?.id,
            command = command
        )
    }

    fun getLocalChapter(chapterId: Long) {
        viewModelScope.launch {
            val chapter = getChapterUseCase.findChapterById(chapterId)
            ttsChapter = chapter
            if (chapter?.isEmpty() == true) {
                ttsSource?.let { source -> getRemoteChapter(chapter) }
            }
            runTTSService(Player.PAUSE)
        }
    }

    fun subscribeChapters(bookId: Long) {
        viewModelScope.launch {
            getChapterUseCase.subscribeChaptersByBookId(
                bookId
            ).collect {
                ttsChapters = it
            }
        }
    }

    private suspend fun getRemoteChapter(
        chapter: Chapter,
    ) {
        val catalog = ttsState.ttsCatalog
        remoteUseCases.getRemoteReadingContent(
            chapter,
            catalog,
            onSuccess = { result ->
                ttsChapter = result
            },
            onError = {

            }
        )
    }
}

