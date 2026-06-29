package eu.kanade.tachiyomi.source

/**
 * Marker interface for novel (text-based) sources.
 *
 * Detection is via the [Source.isNovelSource] property and the text API is [Source.fetchPageText];
 * neither requires this interface. It is kept only for source compatibility with existing
 * extensions that declare `: HttpSource(), NovelSource`. New sources just set
 * `isNovelSource = true` and override [Source.fetchPageText].
 */
@Deprecated("Detection is via Source.isNovelSource; fetchPageText is on Source")
interface NovelSource : Source

/**
 * Checks if this source is a novel source. Backed solely by the [Source.isNovelSource] property,
 * which lives on the shared [Source] interface and is virtual-dispatched across classloaders.
 */
fun Source.isNovelSource(): Boolean = isNovelSource
