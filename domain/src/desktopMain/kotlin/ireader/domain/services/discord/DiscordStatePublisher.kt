package ireader.domain.services.discord

import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Writes the user's current activity to `~/.cache/IReader/discord_state.json` so the
 * standalone `ireader-discord` Python service can pick it up and push Rich Presence
 * to Discord. File-based IPC deliberately keeps IReader itself free of any Discord
 * SDK dependency — the bridge process handles all RPC.
 *
 * JSON schema:
 * ```
 * {
 *   "mode":         "browsing" | "perusing" | "reading" | "tts",
 *   "tab":          string?,   // browsing only (e.g. "Library")
 *   "subTab":       string?,   // browsing, optional (e.g. "Popular")
 *   "book":         string?,   // perusing/reading/tts
 *   "author":       string?,
 *   "cover":        string?,   // raw URL; Python will rehost through catbox
 *   "bookUrl":      string?,   // source URL (book.key) — used for Discord button
 *   "chapter":      string?,   // reading/tts
 *   "chapterIndex": int?,      // 1-based
 *   "chapterCount": int?,
 *   "startedAt":    long       // epoch millis (presence "elapsed" counter start)
 * }
 * ```
 *
 * When [ActivityState.Idle] arrives (app shutdown), the file is **deleted** — that's
 * the signal to Python to call `rpc.clear()` so Discord shows nothing at all. A stale
 * file (no writes for > 30s) is also treated as "IReader closed" by the bridge.
 *
 * Writes are debounced (300 ms) so rapid state changes during navigation don't thrash
 * the filesystem. Atomic rename prevents the Python poller from ever reading a
 * half-written file.
 */
class DiscordStatePublisher(
    private val holder: ActivityStateHolder,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val stateFile: File = run {
        val cacheDir = File(System.getProperty("user.home"), ".cache/IReader")
        cacheDir.mkdirs()
        File(cacheDir, "discord_state.json")
    }
    private val tmpFile = File(stateFile.parentFile, stateFile.name + ".tmp")

    private var writer: Job? = null
    private var heartbeat: Job? = null

    fun start() {
        if (writer?.isActive == true) return
        writer = scope.launch {
            // StateFlow already conflates — if two updates arrive before the collector
            // wakes, only the newest is delivered. collectLatest then gives us debouncing:
            // a new emission cancels the delay and this block re-enters with the newer
            // value, so we only write at most ~3× per second during rapid changes.
            holder.activity.collectLatest { activity ->
                delay(300)
                writeState(activity)
            }
        }
        // Heartbeat: re-write the current activity every 20s while the user is
        // doing anything (reading, listening, browsing, perusing). Without this,
        // a motionless reader session triggers no state changes — the Python
        // bridge then treats the state file as stale (>30s) and clears the
        // presence mid-read. The heartbeat is skipped on Idle so a truly closed
        // IReader session still goes stale and the presence drops.
        heartbeat = scope.launch {
            while (true) {
                delay(20_000)
                val current = holder.activity.value
                if (current !is ActivityState.Idle) {
                    writeState(current)
                }
            }
        }
    }

    fun stop() {
        writer?.cancel()
        heartbeat?.cancel()
        writer = null
        heartbeat = null
        scope.launch { writeState(ActivityState.Idle) }
    }

    private suspend fun writeState(activity: ActivityState) = withContext(Dispatchers.IO) {
        try {
            if (activity is ActivityState.Idle) {
                // Signal "IReader closed" by deleting the file outright — Python
                // treats file-missing as `rpc.clear()` rather than publishing any
                // synthetic "idle" presence.
                stateFile.delete()
                tmpFile.delete()
                return@withContext
            }
            val json = activity.toJson()
            tmpFile.writeText(json)
            // Atomic rename so readers never see a partial file.
            if (!tmpFile.renameTo(stateFile)) {
                stateFile.writeText(json)
                tmpFile.delete()
            }
        } catch (_: Exception) {
            // Never let publisher failure bubble up into the UI.
        }
    }
}

private fun ActivityState.toJson(): String {
    val b = StringBuilder()
    b.append('{')
    when (this) {
        ActivityState.Idle -> {
            b.append("\"mode\":\"idle\"")
        }
        is ActivityState.Browsing -> {
            b.append("\"mode\":\"browsing\"")
            b.append(",\"tab\":").appendJsonString(tab)
            b.append(",\"subTab\":").appendJsonString(subTab)
            b.append(",\"startedAt\":").append(startedAt)
        }
        is ActivityState.Perusing -> {
            b.append("\"mode\":\"perusing\"")
            b.append(",\"book\":").appendJsonString(bookTitle)
            b.append(",\"author\":").appendJsonString(author)
            b.append(",\"cover\":").appendJsonString(coverUrl)
            b.append(",\"bookUrl\":").appendJsonString(bookUrl)
            b.append(",\"startedAt\":").append(startedAt)
        }
        is ActivityState.Reading -> {
            b.append("\"mode\":\"reading\"")
            b.append(",\"book\":").appendJsonString(bookTitle)
            b.append(",\"author\":").appendJsonString(author)
            b.append(",\"cover\":").appendJsonString(coverUrl)
            b.append(",\"bookUrl\":").appendJsonString(bookUrl)
            b.append(",\"chapter\":").appendJsonString(chapterName)
            b.append(",\"chapterIndex\":").append(chapterIndex)
            b.append(",\"chapterCount\":").append(chapterCount)
            b.append(",\"startedAt\":").append(startedAt)
        }
        is ActivityState.ListeningTTS -> {
            b.append("\"mode\":\"tts\"")
            b.append(",\"book\":").appendJsonString(bookTitle)
            b.append(",\"author\":").appendJsonString(author)
            b.append(",\"cover\":").appendJsonString(coverUrl)
            b.append(",\"bookUrl\":").appendJsonString(bookUrl)
            b.append(",\"chapter\":").appendJsonString(chapterName)
            b.append(",\"chapterIndex\":").append(chapterIndex)
            b.append(",\"chapterCount\":").append(chapterCount)
            b.append(",\"startedAt\":").append(startedAt)
        }
    }
    b.append('}')
    return b.toString()
}

private fun StringBuilder.appendJsonString(value: String?) {
    if (value == null) {
        append("null")
        return
    }
    append('"')
    for (c in value) {
        when (c) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            in '\u0000'..'\u001F' -> append("\\u%04x".format(c.code))
            else -> append(c)
        }
    }
    append('"')
}
