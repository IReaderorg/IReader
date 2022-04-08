package org.ireader.presentation.feature_reader.presentation.reader.viewmodel

import org.ireader.presentation.feature_services.notification.DefaultNotificationHelper
import javax.inject.Inject

class TextReaderManager @Inject constructor(
    private val defaultNotificationHelper: DefaultNotificationHelper,
) {


//    val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
//
//
//    fun ReaderScreenViewModel.readText(context: Context, mediaSessionCompat: MediaSessionCompat) {
//
//        if (speaker == null) {
//            speaker = TextToSpeech(context) { status ->
//                ttsIsLoading = true
//                if (status == TextToSpeech.ERROR) {
//                    context.toast("Text-to-Speech Not Available")
//                    return@TextToSpeech
//                }
//                ttsIsLoading = false
//            }
//        }
//        speaker?.let { ttl ->
//            ttl.availableLanguages?.let {
//                languages = it.toList()
//            }
//            ttl.voices?.toList()?.let {
//                voices = it
//            }
//            ttl.voices?.firstOrNull { it.name == currentVoice }?.let {
//                ttl.voice = it
//            }
//            ttl.availableLanguages?.firstOrNull { it.displayName == currentLanguage }
//                ?.let {
//                    ttl.language = it
//                }
//
//            ttl.setPitch(pitch)
//            ttl.setSpeechRate(speechSpeed)
//            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            } else {
//                PendingIntent.FLAG_UPDATE_CURRENT
//            }
//            stateChapter?.let { chapter ->
//                ttsBook?.let { book ->
//                    try {
//                        NotificationManagerCompat.from(context).apply {
//                            viewModelScope.launch {
//                                val builder =
//                                    defaultNotificationHelper.basicPlayingTextReaderNotification(
//                                        chapter,
//                                        book,
//                                        isPlaying,
//                                        currentReadingParagraph,
//                                        mediaSessionCompat)
//
//                                notify(Notifications.ID_TEXT_READER_PROGRESS, builder.build())
//
//                            }
//
//                            ttl.speak(chapter.content[currentReadingParagraph],
//                                TextToSpeech.QUEUE_FLUSH,
//                                null,
//                                currentReadingParagraph.toString())
//
//                            ttl.setOnUtteranceProgressListener(object :
//                                UtteranceProgressListener() {
//                                override fun onStop(
//                                    utteranceId: String?,
//                                    interrupted: Boolean,
//                                ) {
//                                    super.onStop(utteranceId, interrupted)
//
//                                    isPlaying = false
//                                }
//
//                                override fun onStart(p0: String?) {
//                                    // showTextReaderNotification(context)
//                                    isPlaying = true
//                                }
//
//                                override fun onDone(p0: String?) {
//                                    if (currentReadingParagraph < chapter.content.size) {
//                                        currentReadingParagraph += 1
//                                        //  builder.setProgress(chapter.content.size, currentReadingParagraph,false)
//                                        //  notify(Notifications.ID_TEXT_READER_PROGRESS, builder.build())
//                                        readText(context, mediaSessionCompat)
//                                    }
//                                    if (currentReadingParagraph == chapter.content.size && speaker != null && !ttsIsLoading && !isRemoteLoading) {
//                                        isPlaying = false
//                                        speaker?.stop()
//                                        if (autoNextChapter) {
//                                            ttsSource?.let {
//                                                updateChapterSliderIndex(ttsCurrentChapterIndex + 1)
//                                                viewModelScope.launch {
//                                                    getChapter(getCurrentChapterByIndex().id,
//                                                        source = it) {
//                                                        if (chapter.content.isNotEmpty() && !ttsIsLoading && !isRemoteLoading) {
//                                                            readText(context = context,
//                                                                mediaSessionCompat)
//                                                        }
//                                                    }
//                                                }
//
//                                            }
//
//
//                                        }
//                                    }
//                                }
//
//                                override fun onError(p0: String?) {
//                                    isPlaying = false
//                                }
//
//
//                                override fun onBeginSynthesis(
//                                    utteranceId: String?,
//                                    sampleRateInHz: Int,
//                                    audioFormat: Int,
//                                    channelCount: Int,
//                                ) {
//                                    super.onBeginSynthesis(utteranceId,
//                                        sampleRateInHz,
//                                        audioFormat,
//                                        channelCount)
//                                    Timber.e(utteranceId)
//                                }
//                            })
//                        }
//                    } catch (e: Exception) {
//                        Timber.e(e.localizedMessage)
//                    }
//
//                }
//            }
//        }
//    }
//
//    fun ReaderScreenViewModel.releaseMediaSession(context: Context) {
//        speaker?.shutdown()
//        mediaSessionCompat(context).release()
//        NotificationManagerCompat.from(context)
//            .cancel(Notifications.ID_TEXT_READER_PROGRESS)
//    }


}