package ireader.domain.services.discord

import ireader.domain.services.tts_service.v2.TTSController
import ireader.domain.services.tts_service.v2.TTSState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

/**
 * Wires the v2 [TTSController]'s state → [ActivityStateHolder] so Discord Rich
 * Presence reflects TTS playback. The **legacy** `DesktopTTSService` isn't used
 * by the current reader/TTS pipeline — its state stays idle even while v2
 * playback is running — so this bridge has to observe `TTSController.state`
 * directly.
 *
 * Presence rules:
 * - `playbackState == PLAYING` with book+chapter → `setListeningTTS`
 * - any non-playing transition → `releaseTTS` so the reader (or the tab
 *   browsing state) can take over publishing again.
 *
 * Started eagerly at app init from [ireader.domain.di.DomainModule].
 */
class TTSActivityBridge(
    private val ttsController: TTSController,
    private val holder: ActivityStateHolder,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            ttsController.state
                // Narrow: only re-fire when fields that affect the presence change.
                .distinctUntilChangedBy { Key(it.isPlaying, it.book?.id, it.chapter?.id) }
                .collect { snap -> apply(snap) }
        }
    }

    private fun apply(s: TTSState) {
        val book = s.book
        val chapter = s.chapter
        if (s.isPlaying && book != null && chapter != null) {
            holder.setListeningTTS(
                bookTitle = book.title,
                author = book.author.takeIf { it.isNotBlank() },
                coverUrl = book.cover.takeIf { it.isNotBlank() },
                bookUrl = book.key.takeIf {
                    it.startsWith("http://") || it.startsWith("https://")
                },
                chapterName = chapter.name,
                // v2 TTSState doesn't carry chapter index/count — we don't have
                // access to the chapters list here, so publish 1/1 as a
                // placeholder. The reader VM fills these fields when the user
                // returns from TTS to the reader screen.
                chapterIndex = 1,
                chapterCount = 1,
            )
        } else if (!s.isPlaying) {
            holder.releaseTTS()
        }
    }

    private data class Key(val playing: Boolean, val bookId: Long?, val chapterId: Long?)
}
