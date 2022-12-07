package ireader.presentation.ui.home.tts

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
import androidx.lifecycle.viewModelScope
import ireader.domain.models.entities.Chapter
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.tts_service.Player
import ireader.domain.services.tts_service.TTSState
import ireader.domain.services.tts_service.TTSStateImpl
import ireader.domain.services.tts_service.media_player.TTSService
import ireader.domain.services.tts_service.media_player.isPlaying
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.domain.usecases.preferences.reader_preferences.TextReaderPrefUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.utils.extensions.findComponentActivity
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.component.Controller
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TTSViewModel(
    val ttsState: TTSStateImpl,
    private val param: Param,
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
    data class Param(val sourceId:Long? ,val chapterId: Long?, val bookId: Long?,val readingParagraph: String?)

    companion object  {
        fun createParam(controller: Controller) : Param {
            return Param(controller.navBackStackEntry.arguments?.getLong("sourceId"),controller.navBackStackEntry.arguments?.getLong("chapterId"),controller.navBackStackEntry.arguments?.getLong("bookId"),controller.navBackStackEntry.arguments?.getString(
                NavigationArgs.readingParagraph.name))
        }
    }
    var fullScreenMode by mutableStateOf(false)

    val speechRate = readerPreferences.speechRate().asState()
    val speechPitch = readerPreferences.speechPitch().asState()
    val sleepModeUi = readerPreferences.sleepMode().asState()
    val sleepTimeUi = readerPreferences.sleepTime().asState()
    val autoNext = readerPreferences.readerAutoNext().asState()
    val voice = readerPreferences.speechVoice().asState()
    val language = readerPreferences.speechLanguage().asState()
    val isTtsTrackerEnable = readerPreferences.followTTSSpeaker().asState()

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
    val ttsIconAlignments = readerPreferences.ttsIconAlignments().asState()

    private var chapterId: Long = -1
    private var initialize: Boolean = false

    init {
        val sourceId = param.sourceId
        val chapterId = param.chapterId
        val bookId = param.bookId

        kotlin.runCatching {
            val readingParagraph =
                param.readingParagraph
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
