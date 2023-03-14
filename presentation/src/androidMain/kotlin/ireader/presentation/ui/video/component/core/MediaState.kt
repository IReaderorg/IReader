package ireader.presentation.ui.video.component.core


import android.content.Context
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.text.TextRenderer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import com.google.common.net.HttpHeaders.USER_AGENT
import ireader.core.http.HttpClients
import ireader.core.http.UserAgentInterceptor
import ireader.core.http.okhttp
import ireader.core.source.HttpSource
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.MovieUrl
import ireader.core.source.model.Page
import ireader.core.source.model.Subtitle
import ireader.presentation.imageloader.coil.image_loaders.convertToOkHttpRequest
import ireader.presentation.ui.video.component.cores.*
import ireader.presentation.ui.video.component.cores.PlayerSubtitleHelper.Companion.toSubtitleMimeType
import ireader.presentation.ui.video.component.cores.player.SSLTrustManager
import okhttp3.Request
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import java.io.File
import java.net.URI
import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import kotlin.math.absoluteValue


/**
 * Create and [remember] a [MediaState] instance.
 *
 * Changes to [player] will result in the [MediaState] being updated.
 *
 * @param player the value for [MediaState.player]
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun rememberMediaState(
        player: ExoPlayer?,
        context: Context,
): MediaState {
    val di : DI by rememberInstance()
    return remember { MediaState(initPlayer = player, context = context,di) }.apply {
        this.player = player
    }
}


@Stable
class MediaState(
    private val initPlayer: ExoPlayer? = null,
    private val context: Context, override val di: DI,

    ) : DIAware {
    val subtitleHelper : PlayerSubtitleHelper = PlayerSubtitleHelper()
    internal val stateOfPlayerState = mutableStateOf(initPlayer?.state(subtitleHelper))

    var activeSubtitles: State<List<SubtitleData>> = derivedStateOf<List<SubtitleData>> { subtitleHelper.activeSubtitles.value.toList() }
    /**
     * The player to use, or null to detach the current player.
     * Only players which are accessed on the main thread are supported (`
     * player.getApplicationLooper() == Looper.getMainLooper()`).
     */
    var player: ExoPlayer?
        set(current) {
            require(current == null || current.applicationLooper == Looper.getMainLooper()) {
                "Only players which are accessed on the main thread are supported."
            }
            val previous = _player
            if (current !== previous) {
                _player = current
                onPlayerChanged(previous, current)
            }
        }
        get() = _player


    /**
     * The state of the [Media]'s [player].
     */
    val playerState: PlayerState? get() = stateOfPlayerState.value

    // Controller visibility related properties and functions
    /**
     * Whether the controller is showing.
     */
    var isControllerShowing: Boolean
        get() = controllerVisibility.isShowing
        set(value) {
            controllerVisibility = if (value) ControllerVisibility.Visible
            else ControllerVisibility.Invisible
        }
    var medias = emptyList<MovieUrl>()
    var subs = emptyList<Subtitle>()

    /**
     * The current [visibility][ControllerVisibility] of the controller.
     */
    var controllerVisibility: ControllerVisibility by mutableStateOf(ControllerVisibility.Invisible)

    /**
     * Typically, when controller is shown, it will be automatically hidden after a short time has
     * elapsed without user interaction. If [shouldShowControllerIndefinitely] is true, you should
     * consider disabling this behavior, and show the controller indefinitely.
     */
    val shouldShowControllerIndefinitely: Boolean by derivedStateOf {
        playerState?.run {
            controllerAutoShow
                    && !timeline.isEmpty
                    && (playbackState == Player.STATE_IDLE
                    || playbackState == Player.STATE_ENDED
                    || !playWhenReady)
        } ?: true
    }



    internal var controllerAutoShow: Boolean by mutableStateOf(true)

    internal fun maybeShowController() {
        if (shouldShowControllerIndefinitely) {
            controllerVisibility = ControllerVisibility.Visible
        }
    }

    // internally used properties and functions
    private val listener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            closeShutter = false
            artworkPainter = null
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                            Player.EVENT_PLAYBACK_STATE_CHANGED,
                            Player.EVENT_PLAY_WHEN_READY_CHANGED
                    )
            ) {
                maybeShowController()
            }
        }

    }
    private var _player: ExoPlayer? by mutableStateOf(initPlayer)
    private fun onPlayerChanged(previous: Player?, current: ExoPlayer?) {
        previous?.removeListener(listener)
        stateOfPlayerState.value?.dispose()
        stateOfPlayerState.value = current?.state(subtitleHelper)
        current?.addListener(listener)
        if (current == null) {
            controllerVisibility = ControllerVisibility.Invisible
        }
    }



    internal val contentAspectRatioRaw by derivedStateOf {
        artworkPainter?.aspectRatio
                ?: (playerState?.videoSize ?: VideoSize.UNKNOWN).aspectRatio
    }
    private var _contentAspectRatio by mutableStateOf(0f)
    internal var contentAspectRatio
        internal set(value) {
            val aspectDeformation: Float = value / contentAspectRatio - 1f
            if (aspectDeformation.absoluteValue > 0.01f) {
                // Not within the allowed tolerance, populate the new aspectRatio.
                _contentAspectRatio = value
            }
        }
        get() = _contentAspectRatio

    // true: video track is selected
    // false: non video track is selected
    // null: there isn't any track
    internal val isVideoTrackSelected: Boolean? by derivedStateOf {
        playerState?.tracksInfo
                ?.takeIf { it.groups.isNotEmpty() }
                ?.isTypeSelected(C.TRACK_TYPE_VIDEO)
    }

    internal var closeShutter by mutableStateOf(true)

    internal val artworkData: ByteArray? by derivedStateOf {
        playerState?.mediaMetadata?.artworkData
    }
    internal var artworkPainter by mutableStateOf<Painter?>(null)

    internal val playerError: PlaybackException? by derivedStateOf {
        playerState?.playerError
    }

    init {

        initPlayer?.addListener(listener)
    }




    companion object {
         var simpleCacheSize: Long  by mutableStateOf(0L)
         var simpleCache: SimpleCache? by mutableStateOf(null)
        var currentTextRenderer: CustomTextRenderer? by mutableStateOf(null)
    }
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0
    var cacheSize = 0L
    var simpleCacheSize = 0L
    var videoBufferMs = 0L

    var currentSubtitleOffset: Long = 0

    private fun loadExo(
        context: Context,
        mediaSlices: List<MediaItem>,
        subSources: List<SingleSampleMediaSource>,
        cacheFactory: CacheDataSource.Factory? = null
    ) {

        player = buildExoPlayer(
            context,
            mediaSlices,
            subSources,
            currentWindow,
            playbackPosition,
            playBackSpeed =playerState?.playbackSpeed ?: 1F,
            cacheSize = cacheSize,
            videoBufferMs = videoBufferMs,
            playWhenReady = playerState?.isPlaying ?: false, // this keep the current state of the player
            cacheFactory = cacheFactory,
            subtitleOffset = currentSubtitleOffset,
            maxVideoHeight = null,
        )

    }
    @OptIn(UnstableApi::class)
    private fun  buildExoPlayer(
        context: Context,
        mediaItemSlices: List<MediaItem>,
        subSources: List<SingleSampleMediaSource>,
        currentWindow: Int,
        playbackPosition: Long,
        playBackSpeed: Float,
        subtitleOffset: Long,
        cacheSize: Long,
        videoBufferMs: Long,
        playWhenReady: Boolean = true,
        cacheFactory: CacheDataSource.Factory? = null,
        trackSelector: TrackSelector? = null,
        /**
         * Sets the m3u8 preferred video quality, will not force stop anything with higher quality.
         * Does not work if trackSelector is defined.
         **/
        maxVideoHeight: Int? = null
    ): ExoPlayer {
        val exoPlayerBuilder =
            ExoPlayer.Builder(context)
                .setRenderersFactory { eventHandler, videoRendererEventListener, audioRendererEventListener, textRendererOutput, metadataRendererOutput ->
                    DefaultRenderersFactory(context).createRenderers(
                        eventHandler,
                        videoRendererEventListener,
                        audioRendererEventListener,
                        textRendererOutput,
                        metadataRendererOutput
                    ).map {
                        if (it is TextRenderer) {
                            currentTextRenderer = CustomTextRenderer(
                                subtitleOffset,
                                textRendererOutput,
                                eventHandler.looper,
                                CustomSubtitleDecoderFactory()
                            )
                            currentTextRenderer!!
                        } else it
                    }.toTypedArray()
                }
                .setTrackSelector(trackSelector ?: getTrackSelector(context, maxVideoHeight))
                .setLoadControl(
                    DefaultLoadControl.Builder()
                        .setTargetBufferBytes(
                            if (cacheSize <= 0) {
                                DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES
                            } else {
                                if (cacheSize > Int.MAX_VALUE) Int.MAX_VALUE else cacheSize.toInt()
                            }
                        )
                        .setBufferDurationsMs(
                            DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                            if (videoBufferMs <= 0) {
                                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS
                            } else {
                                videoBufferMs.toInt()
                            },
                            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                        ).build()
                )


        val factory =
            if (cacheFactory == null) DefaultMediaSourceFactory(context)
            else DefaultMediaSourceFactory(cacheFactory)

        // If there is only one item then treat it as normal, if multiple: concatenate the items.
        val videoMediaSource = if (mediaItemSlices.size == 1) {
            factory.createMediaSource(mediaItemSlices.first())
        } else {
            val source = ConcatenatingMediaSource()
            mediaItemSlices.map {
                source.addMediaSource(
                    // The duration MUST be known for it to work properly, see https://github.com/google/ExoPlayer/issues/4727
                    ClippingMediaSource(
                        factory.createMediaSource(it),
                        Long.MAX_VALUE
                    )
                )
            }
            source
        }

        println("PLAYBACK POS $playbackPosition")
        return exoPlayerBuilder.build().apply {
            setPlayWhenReady(playWhenReady)
            seekTo(currentWindow, playbackPosition)
            setMediaSource(
                MergingMediaSource(
                    videoMediaSource, *subSources.toTypedArray()
                ),
                playbackPosition
            )
            setHandleAudioBecomingNoisy(true)
            setPlaybackSpeed(playBackSpeed)
        }
    }
    private fun getTrackSelector(context: Context, maxVideoHeight: Int?): TrackSelector {
        val trackSelector = DefaultTrackSelector(context)
        trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(context)
            // .setRendererDisabled(C.TRACK_TYPE_VIDEO, true)
            .setRendererDisabled(C.TRACK_TYPE_TEXT, true)
            // Experimental, I think this causes issues with audio track init 5001
//                .setTunnelingEnabled(true)
            .setDisabledTextTrackSelectionFlags(C.TRACK_TYPE_TEXT)
            // This will not force higher quality videos to fail
            // but will make the m3u8 pick the correct preferred
            .setMaxVideoSize(Int.MAX_VALUE, maxVideoHeight ?: Int.MAX_VALUE)
            .setPreferredAudioLanguage(null)
            .build()
        return trackSelector
    }
    fun setPreferredAudioTrack(trackLanguage: String?) {
        //playerState?.preferredAudioTrackLanguage = trackLanguage
        player.let { exo ->
            exo?.trackSelectionParameters = exo?.trackSelectionParameters!!
                    .buildUpon()
                    .setPreferredAudioLanguage(trackLanguage)
                    .build()!!
        }

    }
    val client : HttpClients by instance<HttpClients>()

    private fun createOnlineSource(headers: Map<String, String>): HttpDataSource.Factory {
        val source = OkHttpDataSource.Factory(client.default.okhttp).setUserAgent(USER_AGENT)
        return source.apply {
            setDefaultRequestProperties(headers)
        }
    }

    private fun createOnlineSource( link: String?,extensions: HttpSource? = null): HttpDataSource.Factory? {
        // this like can prevent from crashing the app when local file was loaded.
        if (link == null || !link.contains("ireader/core/http")) {
            return null
        }
        val okhttp = extensions?.client?.okhttp ?: client.default.okhttp
        val extensionHeaders = extensions?.getImageRequest(ImageUrl(link))?.second?.build()?.convertToOkHttpRequest() ?: Request.Builder().url(link).build()
        val interceptor = UserAgentInterceptor()

        val source = run {
            val client = okhttp.newBuilder()
                    .addInterceptor(interceptor)
                    .build()
            DefaultHttpDataSource.Factory().setUserAgent(USER_AGENT)
        }

        val headers = mapOf(
                "referer" to (extensions?.baseUrl ?: "https://google.com"),
                "accept" to "*/*",
                "sec-ch-ua" to "\"Chromium\";v=\"91\", \" Not;A Brand\";v=\"99\"",
                "sec-ch-ua-mobile" to "?0",
                "sec-fetch-user" to "?1",
                "sec-fetch-mode" to "navigate",
                "sec-fetch-dest" to "video"
        ) + extensionHeaders.headers // Adds the headers from the provider, e.g Authorization

        return source.apply {
            setDefaultRequestProperties(headers)
        }
    }

    private fun Context.createOfflineSource(): DataSource.Factory {
        return DefaultDataSource.Factory(this)
    }

    fun setPreferredSubtitles(subtitle: SubtitleData?): Boolean {
        Log.i("TAG", "setPreferredSubtitles init $subtitle")
        playerState?.currentSubtitle = subtitle

        return (player?.trackSelector as? DefaultTrackSelector?)?.let { trackSelector ->
            val name = subtitle?.name
            if (name.isNullOrBlank()) {
                trackSelector.setParameters(
                        trackSelector.buildUponParameters()
                                .setPreferredTextLanguage(null)
                )
            } else {
                when (subtitleHelper?.subtitleStatus(subtitle)) {
                    SubtitleStatus.REQUIRES_RELOAD -> {
                        return@let true
                    }
                    SubtitleStatus.IS_ACTIVE -> {
                        trackSelector.setParameters(
                                trackSelector.buildUponParameters()
                                        .apply {
                                            if (subtitle.origin == SubtitleOrigin.EMBEDDED_IN_VIDEO)
                                            // The real Language (two letter) is in the url
                                            // No underscore as the .url is the actual exoplayer designated language
                                                setPreferredTextLanguage(subtitle.url)
                                            else
                                                setPreferredTextLanguage("_$name")
                                        }
                        )
                    }
                    else -> {
                        return@let false
                    }
                }
            }
            return false
        } ?: false
    }

    fun reloadPlayer() {
        simpleCache?.release()
        simpleCache = null

        player?.release()
        currentLink?.let {
            loadOnlinePlayer(context,  medias + subs)
        }?: currentDownloadedFile?.let {
            loadOfflinePlayer(context, it)
        }
    }

    private fun getSubSources(
            onlineSourceFactory: HttpDataSource.Factory?,
            offlineSourceFactory: DataSource.Factory?,
            subHelper: PlayerSubtitleHelper,
    ): Pair<List<SingleSampleMediaSource>, List<SubtitleData>> {
        val activeSubtitles = ArrayList<SubtitleData>()
        val subSources = subHelper.allSubtitles.value.mapNotNull { sub ->
            val subConfig = MediaItem.SubtitleConfiguration.Builder(Uri.parse(sub.url))
                    .setMimeType(sub.mimeType)
                    .setLanguage("_${sub.name}")
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()
            when (sub.origin) {
                SubtitleOrigin.DOWNLOADED_FILE -> {
                    if (offlineSourceFactory != null) {
                        activeSubtitles.add(sub)
                        SingleSampleMediaSource.Factory(offlineSourceFactory)
                                .createMediaSource(subConfig, C.TIME_UNSET)
                    } else {
                        null
                    }
                }
                SubtitleOrigin.URL -> {
                    if (onlineSourceFactory != null) {
                        activeSubtitles.add(sub)
                        SingleSampleMediaSource.Factory(onlineSourceFactory.apply {
                            if (sub.headers.isNotEmpty())
                                this.setDefaultRequestProperties(sub.headers)
                        })
                                .createMediaSource(subConfig, C.TIME_UNSET)
                    } else {
                        null
                    }
                }
                SubtitleOrigin.EMBEDDED_IN_VIDEO -> {
                    if (offlineSourceFactory != null) {
                        activeSubtitles.add(sub)
                        SingleSampleMediaSource.Factory(offlineSourceFactory)
                                .createMediaSource(subConfig, C.TIME_UNSET)
                    } else {
                        null
                    }
                }
            }
        }
        return Pair(subSources, activeSubtitles)
    }

    private fun getCache(context: Context, cacheSize: Long): SimpleCache? {
        return try {
            val databaseProvider = StandaloneDatabaseProvider(context)
            SimpleCache(
                    File(
                            context.cacheDir, "exoplayer"
                    ).also { it.deleteOnExit() }, // Ensures always fresh file
                    LeastRecentlyUsedCacheEvictor(cacheSize),
                    databaseProvider
            )
        } catch (e: Exception) {
            ireader.core.log.Log.error(e)
            null
        }
    }
    var currentLink :String? = null
    var currentDownloadedFile :String? = null
    var ignoreSSL = false
    private fun loadOnlinePlayer(context: Context, link: List<Page>) {
        val movies = link.filterIsInstance<MovieUrl>() ?: emptyList()
        val subtitles = link.filterIsInstance<Subtitle>() ?: emptyList()
        Log.i("TAG", "loadOnlinePlayer $link")
        try {
            currentLink = movies.firstOrNull()?.url
            if (currentLink == null) return
            if (ignoreSSL) {
                // Disables ssl check
                val sslContext: SSLContext = SSLContext.getInstance("TLS")
                sslContext.init(null, arrayOf(SSLTrustManager()), SecureRandom())
                sslContext.createSSLEngine()
                HttpsURLConnection.setDefaultHostnameVerifier { _: String, _: SSLSession ->
                    true
                }
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            }

            val mime = if (URI(currentLink).path.endsWith(".m3u8")) {
                MimeTypes.APPLICATION_M3U8
            } else {
                MimeTypes.VIDEO_MP4
            }

            val mediaItems = movies.map {
                getMediaItem(mime, it.url).build()
            }


            val onlineSourceFactory = createOnlineSource(currentLink)
            val offlineSourceFactory = context.createOfflineSource()

            val (subSources, activeSubtitles) = getSubSources(
                onlineSourceFactory = onlineSourceFactory,
                offlineSourceFactory = offlineSourceFactory,
                subtitleHelper
            )

            subtitleHelper.setActiveSubtitles(activeSubtitles)

            if (simpleCache == null)
                simpleCache = getCache(context, simpleCacheSize)

            val cacheFactory = CacheDataSource.Factory().apply {
                simpleCache?.let { setCache(it) }
                setUpstreamDataSourceFactory(onlineSourceFactory)
            }

            loadExo(context, mediaItems, subSources, cacheFactory)
        } catch (e: Exception) {
            Log.e("TAG", "loadOnlinePlayer error", e)
            //playerError?.invoke(e)
        }
    }
    fun loadPlayer(
        sameEpisode: Boolean,
        link: String?,
        data: String?,
        startPosition: Long?,
        subtitles: Set<SubtitleData>,
        subtitle: SubtitleData?,
        autoPlay: Boolean?
    ) {
        if (sameEpisode) {
            saveData()
        } else {
            playerState?.currentSubtitle = subtitle
            playbackPosition = 0
        }

        startPosition?.let {
            playbackPosition = it
        }


        // we want autoplay because of TV and UX
       // isPlaying = autoPlay ?: isPlaying

        // release the current exoplayer and cache
        releasePlayer()
        if (link != null) {
            loadOnlinePlayer(context, listOf(MovieUrl(link)))
        } else if (data != null) {
            loadOfflinePlayer(context, data)
        }
    }
    fun releasePlayer(saveTime: Boolean = true) {

        player?.release()
        //simpleCache?.release()
        currentTextRenderer = null

        player = null
        //simpleCache = null
    }
    private fun loadOfflinePlayer(context: Context, data: String) {
        Log.i("TAG", "loadOfflinePlayer")
        try {
            currentDownloadedFile = data
            val offlineSourceFactory = context.createOfflineSource()
            val onlineSourceFactory = createOnlineSource(emptyMap())

            val (subSources, activeSubtitles) = getSubSources(
                onlineSourceFactory = onlineSourceFactory,
                offlineSourceFactory = offlineSourceFactory,
                subtitleHelper,
            )
            val mediaItem = getMediaItem(MimeTypes.VIDEO_MP4, data).build()
            subtitleHelper.setActiveSubtitles(activeSubtitles)
            loadExo(context, listOf(mediaItem), subSources)
        } catch (e: Exception) {
            Log.e("TAG", "loadOfflinePlayer error", e)

        }
    }

    private fun getMediaItemBuilder(mimeType: String):
            MediaItem.Builder {
        return MediaItem.Builder()
            //Replace needed for android 6.0.0  https://github.com/google/ExoPlayer/issues/5983
            .setMimeType(mimeType)
    }

    private fun getMediaItem(mimeType: String, uri: Uri): MediaItem {
        return getMediaItemBuilder(mimeType).setUri(uri).build()
    }

    private fun getMediaItem(mimeType: String, url: String): MediaItem.Builder {
        return getMediaItemBuilder(mimeType).setUri(url)
    }

    fun saveData() {

        player?.let { exo ->
            playbackPosition = exo.currentPosition
            currentWindow = exo.currentWindowIndex
        }
    }
    fun setActiveSubtitles(subtitles: List<SubtitleData>) {
        subtitleHelper.setAllSubtitles(subtitles)
        subtitleHelper.setActiveSubtitles(subtitles)
    }
}

/**
 * The visibility state of the controller.
 */
enum class ControllerVisibility(
        val isShowing: Boolean,
) {
    /**
     * All UI controls are visible.
     */
    Visible(true),

    /**
     * A part of UI controls are visible.
     */
    PartiallyVisible(true),

    /**
     * All UI controls are hidden.
     */
    Invisible(false)
}

private val VideoSize.aspectRatio
    get() = if (height == 0) 0f else width * pixelWidthHeightRatio / height
private val Painter.aspectRatio
    get() = intrinsicSize.run {
        if (this == Size.Unspecified || width.isNaN() || height.isNaN() || height == 0f) 0f
        else width / height
    }


fun Subtitle.toSubtitleData() : SubtitleData {
    val origin = if (url.contains("ireader/core/http")) {
        SubtitleOrigin.URL
    } else {
        SubtitleOrigin.DOWNLOADED_FILE
    }
    return SubtitleData(
        name = url.substringBeforeLast(".").substringAfterLast("/"),
        url = url,
        origin,
        url.toSubtitleMimeType(),
        emptyMap()
    )
}