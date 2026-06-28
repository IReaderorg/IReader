package eu.kanade.tachiyomi.source

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Optional interface a [Source] can implement to react to user reading/library events.
 *
 * These are gated by the user's "minimum chapters before
 * tracking" preference.
 * Failures should be raised as exceptions; the dispatcher logs them and shows a toast.
 *
 * Most extensions don't need this — only those backed by a remote service that wants
 * to mirror local reading state.
 *
 * @since extensions-lib 1.6
 */
interface SourceTracker {

    /** Whether this source wants chapter read/unread callbacks. Default true. */
    val supportsChapterTracking: Boolean
        get() = true

    /** Whether this source wants favorite/unfavorite callbacks. Default false. */
    val supportsFavoritesTracking: Boolean
        get() = false

    /**
     * Called after chapters are marked read.
     *
     * @param manga the manga whose chapters were touched.
     * @param changedChapters chapters whose read state flipped to read in this batch.
     *                        May contain arbitrary numbers (e.g. 1, 3, 4) — not just the latest.
     * @param allChapters every chapter the app knows about for this manga, with current
     *                    [SChapter.read] state populated. Use this if you need full
     *                    "marked vs unmarked" context.
     * @param categories names of categories this manga belongs to. Empty list if the
     *                   manga has no user-named categories (e.g. only in Default).
     */
    suspend fun onChaptersRead(
        manga: SManga,
        changedChapters: List<SChapter>,
        allChapters: List<SChapter>,
        categories: List<String>,
    ) = Unit

    /** Called after chapters are marked unread. Same parameters as [onChaptersRead]. */
    suspend fun onChaptersUnread(
        manga: SManga,
        changedChapters: List<SChapter>,
        allChapters: List<SChapter>,
        categories: List<String>,
    ) = Unit

    /** Called after the user added this manga to the library. */
    suspend fun onFavorited(
        manga: SManga,
        categories: List<String>,
    ) = Unit

    /** Called after the user removed this manga from the library. */
    suspend fun onUnfavorited(
        manga: SManga,
        categories: List<String>,
    ) = Unit
}

/**
 * True when [Source] implements [SourceTracker] under either classloader.
 */
fun Source.isSourceTracker(): Boolean {
    if (this is SourceTracker) return true
    return this::class.java.allInterfaceNames()
        .contains("eu.kanade.tachiyomi.source.SourceTracker")
}

/**
 * Reads a Boolean property on [Source] via reflection. Used for the optional
 * `supportsChapterTracking` / `supportsFavoritesTracking` flags so the dispatcher
 * doesn't have to cast the source to its own SourceTracker class.
 */
fun Source.sourceTrackerBoolean(propertyName: String, default: Boolean): Boolean {
    if (this is SourceTracker) {
        return when (propertyName) {
            "supportsChapterTracking" -> this.supportsChapterTracking
            "supportsFavoritesTracking" -> this.supportsFavoritesTracking
            else -> default
        }
    }
    return try {
        // Try `getSupportsChapterTracking()` Kotlin property accessor first, then the bare name.
        val getter = "get" + propertyName.replaceFirstChar { it.uppercase() }
        val method = runCatching { this::class.java.getMethod(getter) }.getOrNull()
            ?: runCatching { this::class.java.getMethod(propertyName) }.getOrNull()
            ?: return default
        method.invoke(this) as? Boolean ?: default
    } catch (_: Exception) {
        default
    }
}

/**
 * Invokes one of the four [SourceTracker] callbacks by reflection so the call
 * works across classloaders. Same param list as the interface methods.
 */
@Suppress("UNCHECKED_CAST")
suspend fun Source.invokeSourceTrackerCallback(
    method: SourceTrackerMethod,
    manga: SManga,
    changedChapters: List<SChapter>,
    allChapters: List<SChapter>,
    categories: List<String>,
) {
    if (this is SourceTracker) {
        when (method) {
            SourceTrackerMethod.ON_CHAPTERS_READ -> onChaptersRead(manga, changedChapters, allChapters, categories)
            SourceTrackerMethod.ON_CHAPTERS_UNREAD -> onChaptersUnread(manga, changedChapters, allChapters, categories)
            SourceTrackerMethod.ON_FAVORITED -> onFavorited(manga, categories)
            SourceTrackerMethod.ON_UNFAVORITED -> onUnfavorited(manga, categories)
        }
        return
    }
    suspendCoroutine<Unit> { continuation ->
        try {
            val cls = this::class.java
            val reflectMethod = when (method) {
                SourceTrackerMethod.ON_CHAPTERS_READ,
                SourceTrackerMethod.ON_CHAPTERS_UNREAD,
                -> cls.getMethod(
                    method.methodName,
                    SManga::class.java,
                    List::class.java,
                    List::class.java,
                    List::class.java,
                    Continuation::class.java,
                )

                SourceTrackerMethod.ON_FAVORITED,
                SourceTrackerMethod.ON_UNFAVORITED,
                -> cls.getMethod(
                    method.methodName,
                    SManga::class.java,
                    List::class.java,
                    Continuation::class.java,
                )
            }
            val callback = object : Continuation<Any?> {
                override val context = continuation.context
                override fun resumeWith(result: Result<Any?>) {
                    result.fold(
                        onSuccess = { continuation.resume(Unit) },
                        onFailure = { continuation.resumeWithException(it) },
                    )
                }
            }
            val args: Array<Any?> = when (method) {
                SourceTrackerMethod.ON_CHAPTERS_READ,
                SourceTrackerMethod.ON_CHAPTERS_UNREAD,
                -> arrayOf(manga, changedChapters, allChapters, categories, callback)

                SourceTrackerMethod.ON_FAVORITED,
                SourceTrackerMethod.ON_UNFAVORITED,
                -> arrayOf(manga, categories, callback)
            }
            val result = reflectMethod.invoke(this, *args)
            if (result != kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED) {
                continuation.resume(Unit)
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}

enum class SourceTrackerMethod(val methodName: String) {
    ON_CHAPTERS_READ("onChaptersRead"),
    ON_CHAPTERS_UNREAD("onChaptersUnread"),
    ON_FAVORITED("onFavorited"),
    ON_UNFAVORITED("onUnfavorited"),
}

private fun Class<*>.allInterfaceNames(): List<String> {
    val result = mutableListOf<String>()
    var current: Class<*>? = this
    while (current != null) {
        current.interfaces.forEach { collectInterfaces(it, result) }
        current = current.superclass
    }
    return result
}

private fun collectInterfaces(iface: Class<*>, out: MutableList<String>) {
    out += iface.name
    iface.interfaces.forEach { collectInterfaces(it, out) }
}
