package org.ireader.tts

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ireader.common_extensions.findComponentActivity
import org.ireader.common_models.entities.Chapter
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.services.tts_service.Player
import org.ireader.domain.services.tts_service.TTSState
import org.ireader.domain.services.tts_service.TTSStateImpl
import org.ireader.domain.services.tts_service.media_player.TTSService
import org.ireader.domain.services.tts_service.media_player.isPlaying
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
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
    private val readerUseCases: ReaderPrefUseCases,
    private val readerPreferences: ReaderPreferences,
    private val insertUseCases: LocalInsertUseCases,
) : BaseViewModel(),
    TTSState by ttsState {

    var fullScreenMode by mutableStateOf(false)

    val speechRate = readerPreferences.speechRate().asState()
    val speechPitch = readerPreferences.speechPitch().asState()
    val sleepModeUi = readerPreferences.sleepMode().asState()
    val sleepTimeUi = readerPreferences.sleepTime().asState()
    val autoNext = readerPreferences.readerAutoNext().asState()
    val voice = readerPreferences.speechVoice().asState()
    val language = readerPreferences.speechLanguage().asState()

    val theme = readerPreferences.backgroundColorTTS().asState()

    // val textColor = readerPreferences.textColorReader().asState()
    val lineHeight = readerPreferences.lineHeight().asState()
    val betweenLetterSpaces = readerPreferences.betweenLetterSpaces().asState()
    val textWeight = readerPreferences.textWeight().asState()
    val paragraphsIndent = readerPreferences.paragraphIndent().asState()
    val paragraphDistance = readerPreferences.paragraphDistance().asState()
    val textAlignment = readerPreferences.textAlign().asState()
    val font = readerPreferences.font().asState()
    val fontSize = readerPreferences.fontSize().asState()

    private var chapterId: Long = -1
    private var initialize: Boolean = false

    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)

        kotlin.runCatching {
            val readingParagraph =
                savedStateHandle.get<String>(NavigationArgs.readingParagraph.name)
            if (readingParagraph != null && readingParagraph.toInt() < (
                    ttsState.ttsContent?.value?.lastIndex
                        ?: 0
                    )
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
                    /**
                     * ERROR: this line throws an error when devices doesn't have tts service installed
                     */
                    kotlin.runCatching {
                        ttsState.languages = textReader?.availableLanguages?.toList() ?: emptyList()
                        ttsState.voices = textReader?.voices?.toList() ?: emptyList()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        kotlin.runCatching {
            browser?.disconnect()
            textReader?.shutdown()
        }
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
            isServiceConnected = false
            ctrlCallback?.let { controller?.unregisterCallback(it) }
            controller = null
            browser = null
            textReader = null
        }

        override fun onConnectionFailed() {
            isServiceConnected = false
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
                    getChapterUseCase.findChapterById(chapterId)?.let {
                        ttsChapter = it

                    }
                }
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            if (state == null) return
            isPlaying = when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    true
                }
                else -> {
                    false
                }
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
            getChapterUseCase.findChapterById(chapterId)?.let { chapter ->
                ttsChapter = chapter
                if (chapter.isEmpty()) {
                    ttsSource?.let { getRemoteChapter(chapter) }
                }
                runTTSService(Player.PAUSE)
            }

        }
    }

    private fun subscribeChapters(bookId: Long) {
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
                insertUseCases.insertChapter(result)
            },
            onError = {
            }
        )
    }

    fun play(context:Context) {
        if (controller?.playbackState?.state == PlaybackStateCompat.STATE_NONE) {
            initMedia(context)
            initController()
            runTTSService(Player.PLAY)
        } else if (controller?.playbackState?.isPlaying == true) {
            controller?.transportControls?.pause()
        } else {
            controller?.transportControls?.play()
        }
    }
}
