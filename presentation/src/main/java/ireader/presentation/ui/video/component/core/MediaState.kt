package ireader.presentation.ui.video.component.core

import android.content.Context
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.media3.common.*
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.text.TextRenderer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.google.common.net.HttpHeaders
import com.google.common.net.HttpHeaders.USER_AGENT
import ireader.core.http.UserAgentInterceptor
import ireader.core.http.okhttp
import ireader.core.source.HttpSource
import ireader.core.source.model.ImageUrl
import ireader.presentation.imageloader.coil.image_loaders.convertToOkHttpRequest
import ireader.presentation.ui.video.component.cores.*
import org.koin.core.annotation.Factory
import java.io.File
import kotlin.math.absoluteValue


/**
 * Create and [remember] a [MediaState] instance.
 *
 * Changes to [player] will result in the [MediaState] being updated.
 *
 * @param player the value for [MediaState.player]
 */
@Composable
fun rememberMediaState(
        player: ExoPlayer?,
        source: HttpSource?,
        context: Context,
): MediaState = remember { MediaState(initPlayer = player, context = context) }.apply {
    this.player = player
}


/**
 * A state object that can be hoisted to control and observe changes for [Media].
 */
@Factory
@Stable
class MediaState(
        private val initPlayer: ExoPlayer? = null,
        private val context: Context
) {
    internal val stateOfPlayerState = mutableStateOf(initPlayer?.state())
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

    val subtitleHelper : PlayerSubtitleHelper = PlayerSubtitleHelper()

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
        stateOfPlayerState.value = current?.state()
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



    fun createPlayer(source: HttpSource?): ExoPlayer? {

        val offlineSourceFactory = context.createOfflineSource()
        val onlineSourceFactory = source?.let { createOnlineSource(emptyMap(), it) }

        val (subSources, activeSubtitles) = getSubSources(
                onlineSourceFactory = onlineSourceFactory,
                offlineSourceFactory = offlineSourceFactory,
                subtitleHelper,
        )
        player = init(context,onlineSourceFactory, offlineSourceFactory, emptyList(), emptyList())

        return player
    }

    companion object {
         var simpleCacheSize: Long  by mutableStateOf(0L)
         var simpleCache: SimpleCache? by mutableStateOf(null)
        var currentTextRenderer: CustomTextRenderer? by mutableStateOf(null)
    }

    fun init(
            context: Context,
            onlineSourceFactory: HttpDataSource.Factory?,
            createOfflineSource: DataSource.Factory,
            subSources: List<SingleSampleMediaSource>,
            mediaItemSlices: List<MediaItem>,
    ): ExoPlayer {

        var simpleCache = simpleCache
        if (simpleCache == null)
            simpleCache = getCache(context, simpleCacheSize)

        val cacheFactory = CacheDataSource.Factory().apply {
            simpleCache?.let { setCache(it) }
            setUpstreamDataSourceFactory(onlineSourceFactory)
        }
        val builder = ExoPlayer.Builder(context)
        builder.apply {
            setMediaSourceFactory(ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context)))
            setHandleAudioBecomingNoisy(true)
            setTrackSelector(DefaultTrackSelector(context))
            setRenderersFactory { eventHandler, videoRendererEventListener, audioRendererEventListener, textRendererOutput, metadataRendererOutput ->
                DefaultRenderersFactory(context).createRenderers(
                        eventHandler,
                        videoRendererEventListener,
                        audioRendererEventListener,
                        textRendererOutput,
                        metadataRendererOutput
                ).map {
                    if (it is TextRenderer) {
                        currentTextRenderer = CustomTextRenderer(
                                0,
                                textRendererOutput,
                                eventHandler.looper,
                                CustomSubtitleDecoderFactory()
                        )
                        currentTextRenderer!!
                    } else it
                }.toTypedArray()
            }
        }
        val factory =
                DefaultMediaSourceFactory(cacheFactory)

        val videoMediaSource = if (mediaItemSlices.size == 1) {
            factory.createMediaSource(mediaItemSlices.first())
        } else {
            val source = ConcatenatingMediaSource()
            mediaItemSlices.map {
                source.addMediaSource(
                        // The duration MUST be known for it to work properly, see https://github.com/google/ExoPlayer/issues/4727
                        ClippingMediaSource(
                                factory.createMediaSource(it),
                                0L
                        )
                )
            }
            source
        }

        return builder.build().apply {
            playWhenReady = true
            setMediaSource(
                    MergingMediaSource(
                            videoMediaSource, *subSources.toTypedArray()
                    ),
                    0
            )
            trackSelector!!.parameters = DefaultTrackSelector(context)
                    .buildUponParameters()
                    .setSelectUndeterminedTextLanguage(true)
                    .setPreferredTextLanguage(null)
                    .setPreferredAudioLanguage("ko")
                    .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
                    .setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()
        }

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

    private fun createOnlineSource(headers: Map<String, String>, extensions: HttpSource): HttpDataSource.Factory {
        val source = OkHttpDataSource.Factory(extensions.client.okhttp).setUserAgent(USER_AGENT)
        return source.apply {
            setDefaultRequestProperties(headers)
        }
    }

    private fun createOnlineSource(extensions: HttpSource, link: String): HttpDataSource.Factory {
        val okhttp = extensions.client.okhttp
        val extensionHeaders = extensions.getImageRequest(ImageUrl(link)).second.build().convertToOkHttpRequest()
        val interceptor = UserAgentInterceptor()

        val source = run {
            val client = okhttp.newBuilder()
                    .addInterceptor(interceptor)
                    .build()
            DefaultHttpDataSource.Factory().setUserAgent(HttpHeaders.USER_AGENT)
        }

        val headers = mapOf(
                "referer" to extensions.baseUrl,
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

    fun setPreferredSubtitles(subtitle: ireader.presentation.ui.video.component.cores.SubtitleData?): Boolean {
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
                when (playerState?.subtitleHelper?.subtitleStatus(subtitle)) {
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

    fun resetPlayer(setMediaItem: () -> Unit = {}) {
        player?.stop()
        //player = null
        //player = createPlayer()
        setMediaItem()
        playerState?.player?.prepare()
        playerState?.player?.play()
    }

    private fun getSubSources(
            onlineSourceFactory: HttpDataSource.Factory?,
            offlineSourceFactory: DataSource.Factory?,
            subHelper: PlayerSubtitleHelper,
    ): Pair<List<SingleSampleMediaSource>, List<SubtitleData>> {
        val activeSubtitles = ArrayList<SubtitleData>()
        val subSources = subHelper.getAllSubtitles().mapNotNull { sub ->
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
