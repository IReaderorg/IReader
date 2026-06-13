package ireader.domain.services.discord

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Cross-platform singleton tracking the user's current activity for the Discord Rich
 * Presence pipeline. Hook sites (main tabs, book detail, reader, TTS) push updates
 * here; platform publishers observe and forward. Platforms without a publisher ignore
 * the flow entirely.
 *
 * Five activity states:
 *   - [ActivityState.Browsing]  — in a main tab (Library / Updates / History / …)
 *   - [ActivityState.Perusing]  — on a book detail page (cover + title)
 *   - [ActivityState.Reading]   — in the reader actively reading a chapter
 *   - [ActivityState.ListeningTTS] — TTS playback active
 *   - [ActivityState.Idle]      — emitted only at app shutdown; the publisher treats
 *     this as "delete the state file" so Discord's presence clears entirely.
 *
 * Precedence: TTS > Reading. Browsing and Perusing don't self-gate; the most recent
 * setter wins because navigation is inherently sequential.
 */
class ActivityStateHolder {
    private val _activity = MutableStateFlow<ActivityState>(ActivityState.Idle)
    val activity: StateFlow<ActivityState> = _activity.asStateFlow()

    // Precedence flag. When true, `setReading` becomes a no-op so the reader's
    // frequent state ticks (font-size changes, scroll, etc.) don't clobber the
    // TTS activity. Cleared when TTS stops or is released.
    private var ttsLocked: Boolean = false

    // Cache of the last Browsing state. When transient screens (reader, book
    // detail) unmount, they call `restoreBrowsing()` so the presence flips back
    // to the library/tab the user was on — this avoids a dead gap where no one
    // is publishing (the main screen was already composed and its LaunchedEffect
    // won't re-fire on back-navigation).
    private var lastBrowsing: ActivityState.Browsing? = null

    fun setBrowsing(tab: String, subTab: String?) {
        if (ttsLocked) return
        val current = _activity.value
        if (current is ActivityState.Browsing && current.tab == tab && current.subTab == subTab) {
            return // no-op — avoids spurious StateFlow emissions on recomposition
        }
        val startedAt = if (current is ActivityState.Browsing) current.startedAt else nowMillis()
        val newState = ActivityState.Browsing(tab = tab, subTab = subTab, startedAt = startedAt)
        lastBrowsing = newState
        _activity.value = newState
    }

    /**
     * Restore the last known Browsing state. Called from transient screens'
     * onCleared/onDispose hooks (reader VM, book detail VM) when they pop back to
     * a main tab; the main tab's composable won't re-fire its LaunchedEffect
     * because it never actually unmounted.
     */
    fun restoreBrowsing() {
        if (ttsLocked) return
        val restore = lastBrowsing ?: ActivityState.Browsing(tab = "Library", subTab = null, startedAt = nowMillis())
        _activity.value = restore
    }

    fun setPerusing(
        bookTitle: String,
        author: String?,
        coverUrl: String?,
        bookUrl: String?,
    ) {
        if (ttsLocked) return
        val current = _activity.value
        if (current is ActivityState.Perusing && current.bookTitle == bookTitle) return
        val startedAt = if (current is ActivityState.Perusing && current.bookTitle == bookTitle) {
            current.startedAt
        } else {
            nowMillis()
        }
        _activity.value = ActivityState.Perusing(
            bookTitle = bookTitle,
            author = author,
            coverUrl = coverUrl,
            bookUrl = bookUrl,
            startedAt = startedAt,
        )
    }

    fun setReading(
        bookTitle: String,
        author: String?,
        coverUrl: String?,
        bookUrl: String?,
        chapterName: String,
        chapterIndex: Int,
        chapterCount: Int,
    ) {
        if (ttsLocked) return
        val current = _activity.value
        val startedAt = if (current is ActivityState.Reading && current.bookTitle == bookTitle) {
            current.startedAt
        } else {
            nowMillis()
        }
        _activity.value = ActivityState.Reading(
            bookTitle = bookTitle,
            author = author,
            coverUrl = coverUrl,
            bookUrl = bookUrl,
            chapterName = chapterName,
            chapterIndex = chapterIndex,
            chapterCount = chapterCount,
            startedAt = startedAt,
        )
    }

    fun setListeningTTS(
        bookTitle: String,
        author: String?,
        coverUrl: String?,
        bookUrl: String?,
        chapterName: String,
        chapterIndex: Int,
        chapterCount: Int,
    ) {
        ttsLocked = true
        val current = _activity.value
        val startedAt = if (current is ActivityState.ListeningTTS && current.bookTitle == bookTitle) {
            current.startedAt
        } else {
            nowMillis()
        }
        _activity.value = ActivityState.ListeningTTS(
            bookTitle = bookTitle,
            author = author,
            coverUrl = coverUrl,
            bookUrl = bookUrl,
            chapterName = chapterName,
            chapterIndex = chapterIndex,
            chapterCount = chapterCount,
            startedAt = startedAt,
        )
    }

    /** Called when TTS stops. Releases the TTS lock so reader/browse activity can publish again. */
    fun releaseTTS() {
        ttsLocked = false
    }

    /** Emitted only at app shutdown so the publisher wipes the state file (clears presence entirely). */
    fun setIdle() {
        ttsLocked = false
        if (_activity.value != ActivityState.Idle) {
            _activity.value = ActivityState.Idle
        }
    }
}

sealed interface ActivityState {
    /** Publisher sentinel: delete the state file, Python bridge will `rpc.clear()`. */
    data object Idle : ActivityState

    data class Browsing(
        val tab: String,
        val subTab: String?,
        val startedAt: Long,
    ) : ActivityState

    data class Perusing(
        val bookTitle: String,
        val author: String?,
        val coverUrl: String?,
        val bookUrl: String?,
        val startedAt: Long,
    ) : ActivityState

    data class Reading(
        val bookTitle: String,
        val author: String?,
        val coverUrl: String?,
        val bookUrl: String?,
        val chapterName: String,
        val chapterIndex: Int,
        val chapterCount: Int,
        val startedAt: Long,
    ) : ActivityState

    data class ListeningTTS(
        val bookTitle: String,
        val author: String?,
        val coverUrl: String?,
        val bookUrl: String?,
        val chapterName: String,
        val chapterIndex: Int,
        val chapterCount: Int,
        val startedAt: Long,
    ) : ActivityState
}

internal expect fun nowMillis(): Long
