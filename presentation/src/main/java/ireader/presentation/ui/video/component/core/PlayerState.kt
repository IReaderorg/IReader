package ireader.presentation.ui.video.component.core

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.*
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.exoplayer.ExoPlayer
import ireader.core.log.Log
import ireader.presentation.ui.video.component.cores.*
import ireader.presentation.ui.video.component.cores.player.SubtitleHelper

/**
 * Create a instance of [PlayerState] and register a [listener][Player.Listener] to the [Player] to
 * observe its states.
 *
 * NOTE: Should call [dispose][PlayerState.dispose] to unregister the listener to avoid leaking this
 * instance when it is no longer used.
 */
fun ExoPlayer.state(): PlayerState {
    return PlayerStateImpl(this)
}

/**
 * A state object that can be used to observe the [player]'s states.
 */
interface PlayerState {
    val player: ExoPlayer

    val subtitleHelper : PlayerSubtitleHelper

    val timeline: Timeline

    val mediaItemIndex: Int

    val tracksInfo: Tracks

    val mediaMetadata: MediaMetadata

    val playlistMetadata: MediaMetadata

    val isLoading: Boolean

    val availableCommands: Player.Commands

    val trackSelectionParameters: TrackSelectionParameters

    @get:Player.State
    val playbackState: Int

    val playWhenReady: Boolean

    @get:Player.PlaybackSuppressionReason
    val playbackSuppressionReason: Int

    val isPlaying: Boolean
    var playbackSpeed: Float

    @get:Player.RepeatMode
    val repeatMode: Int

    val shuffleModeEnabled: Boolean

    val playerError: PlaybackException?

    val playbackParameters: PlaybackParameters

    val seekBackIncrement: Long

    val seekForwardIncrement: Long

    val maxSeekToPreviousPosition: Long

    val audioAttributes: AudioAttributes

    val selectedAudioTrack: String?

    val volume: Float

    val deviceInfo: DeviceInfo

    val deviceVolume: Int

    val isDeviceMuted: Boolean
    var isFulLScreen: Boolean

    val videoSize: VideoSize

    val cues: List<Cue>
    val cueGroup: List<CueGroup>

    val currentTracks: CurrentTracks?

    var currentSubtitles: List<SubtitleData>?
    var currentSubtitle: SubtitleData?
    var localSubtitles: List<SubtitleData>



    fun dispose()
}

internal class PlayerStateImpl(
        override val player: ExoPlayer
) : PlayerState {
    override val subtitleHelper: PlayerSubtitleHelper = PlayerSubtitleHelper()
    override var timeline: Timeline by mutableStateOf(player.currentTimeline)
        private set

    override var mediaItemIndex: Int by mutableStateOf(player.currentMediaItemIndex)
        private set

    override var tracksInfo: Tracks by mutableStateOf(player.currentTracks)
        private set

    override var mediaMetadata: MediaMetadata by mutableStateOf(player.mediaMetadata)
        private set

    override var playlistMetadata: MediaMetadata by mutableStateOf(player.playlistMetadata)
        private set

    override var isLoading: Boolean by mutableStateOf(player.isLoading)
        private set

    override var availableCommands: Player.Commands by mutableStateOf(player.availableCommands)
        private set

    override var trackSelectionParameters: TrackSelectionParameters by mutableStateOf(player.trackSelectionParameters)
        private set

    @get:Player.State
    override var playbackState: Int by mutableStateOf(player.playbackState)
        private set

    override var playWhenReady: Boolean by mutableStateOf(player.playWhenReady)
        private set

    @get:Player.PlaybackSuppressionReason
    override var playbackSuppressionReason: Int by mutableStateOf(player.playbackSuppressionReason)
        private set

    override var isPlaying: Boolean by mutableStateOf(player.isPlaying)
        private set

    override var playbackSpeed: Float by mutableStateOf(1f)


    @get:Player.RepeatMode
    override var repeatMode: Int by mutableStateOf(player.repeatMode)
        private set

    override var shuffleModeEnabled: Boolean by mutableStateOf(player.shuffleModeEnabled)
        private set

    override var playerError: PlaybackException? by mutableStateOf(player.playerError)
        private set

    override var playbackParameters: PlaybackParameters by mutableStateOf(player.playbackParameters)
        private set

    override var seekBackIncrement: Long by mutableStateOf(player.seekBackIncrement)
        private set

    override var seekForwardIncrement: Long by mutableStateOf(player.seekForwardIncrement)
        private set

    override var maxSeekToPreviousPosition: Long by mutableStateOf(player.maxSeekToPreviousPosition)
        private set

    override var audioAttributes: AudioAttributes by mutableStateOf(player.audioAttributes)
        private set

    override val selectedAudioTrack: String? by derivedStateOf { currentTracks?.currentAudioTrack?.label }

    override var volume: Float by mutableStateOf(player.volume)
        private set

    override var deviceInfo: DeviceInfo by mutableStateOf(player.deviceInfo)
        private set

    override var deviceVolume: Int by mutableStateOf(player.deviceVolume)
        private set

    override var isDeviceMuted: Boolean by mutableStateOf(player.isDeviceMuted)
        private set
    override var isFulLScreen: Boolean by mutableStateOf(true)

    override var videoSize: VideoSize by mutableStateOf(player.videoSize)
        private set

    override var cues: List<Cue> by mutableStateOf(player.currentCues.cues)
        private set
    override var cueGroup: List<CueGroup> by mutableStateOf(listOf(player.currentCues))
        private set

    override var currentTracks: CurrentTracks? by mutableStateOf<CurrentTracks?>(null)
        private set

    override var currentSubtitles: List<SubtitleData>? by mutableStateOf<List<SubtitleData>?>(null)
    override var currentSubtitle: SubtitleData? by mutableStateOf<SubtitleData?>(null)
    override var localSubtitles: List<SubtitleData> by mutableStateOf<List<SubtitleData>>(emptyList())



    /**
     * get all information about tracks
     */
    fun getVideoTracks(tracks: Tracks): CurrentTracks {
        val allTracks = tracks.groups
        val videoTracks = allTracks.mapIndexed { index, group ->
            group.getTrackFormat(0)
        }.filter { it.sampleMimeType?.contains("video/") == true }
                .map { it.toVideoTrack() }
        val audioTracks = allTracks.mapIndexed { index, group ->
            group.getTrackFormat(0)
        }.filter { it.sampleMimeType?.contains("audio/") == true }
                .map { it.toAudioTrack() }

        val selected = allTracks.filter { it.isSelected }.map { it.getTrackFormat(0) }.map { it.toString() }
        Log.error {  "selected tracks: " + selected}

        val embeddedSubtitles =  allTracks.filter { it.type == C.TRACK_TYPE_TEXT }.mapIndexed { index, group ->
            group.getTrackFormat(0)
        }
        fun Format.isSubtitle(): Boolean {
            return this.sampleMimeType?.contains("video/") == false &&
                    this.sampleMimeType?.contains("audio/") == false
        }


        val exoPlayerSelectedTracks =
                tracksInfo.groups.mapNotNull {
                    val format = it.getTrackFormat(0)
                    if (format.isSubtitle())
                        format.language?.let { lang -> lang to it.isSelected }
                    else null
                }

        currentSubtitles = tracksInfo.groups.map {
            // Filter out unsupported tracks
            it.getTrackFormat(0)

        }.mapNotNull {
            // Filter out non subs, already used subs and subs without languages
            if (!it.isSubtitle() ||
                    // Anything starting with - is not embedded
                    it.language?.startsWith("-") == true ||
                    it.language == null
            ) return@mapNotNull null
            return@mapNotNull SubtitleData(
                    // Nicer looking displayed names
                    SubtitleHelper.fromTwoLettersToLanguage(it.language!!) ?: it.language!!,
                    // See setPreferredTextLanguage
                    it.language!!,
                    SubtitleOrigin.EMBEDDED_IN_VIDEO,
                    it.sampleMimeType ?: MimeTypes.APPLICATION_SUBRIP,
                    emptyMap()
            )
        }
        subtitleHelper.setActiveSubtitles((currentSubtitles ?: emptySet()).toSet() ?: emptySet())
        subtitleHelper.setAllSubtitles((currentSubtitles ?: emptySet()).toSet() ?: emptySet())
        Log.error {  "Video tracks: " + player.videoFormat}
        Log.error {  "Audio tracks: " + player.audioFormat}
        return CurrentTracks(
                currentVideoTrack = player.videoFormat?.toVideoTrack(),
                currentAudioTrack = player.audioFormat?.toAudioTrack(),
                videoTracks,
                audioTracks
        )
    }

    private fun getLanguage(): List<String> {
        return player.currentTracks.groups.map {
            it.getTrackFormat(0).language ?: "Unknown Langauge"
        }
    }

    private fun Format.toAudioTrack(): AudioTrack {
        return AudioTrack(
                this.id,
                this.label,
                this.language
        )
    }


    private fun Format.toVideoTrack(): VideoTrack {
        return VideoTrack(
                this.id,
                this.label,
                this.language,
                this.width,
                this.height
        )
    }


    private val listener = object : Player.Listener {


        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            this@PlayerStateImpl.timeline = timeline
            this@PlayerStateImpl.mediaItemIndex = player.currentMediaItemIndex
        }

        override fun onTracksChanged(tracks: Tracks) {
            this@PlayerStateImpl.tracksInfo = tracks
            this@PlayerStateImpl.currentTracks = getVideoTracks(tracks)

        }


        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            this@PlayerStateImpl.mediaMetadata = mediaMetadata
        }

        override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
            this@PlayerStateImpl.playlistMetadata = mediaMetadata
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            this@PlayerStateImpl.isLoading = isLoading
        }

        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            this@PlayerStateImpl.availableCommands = availableCommands
        }

        override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
            this@PlayerStateImpl.trackSelectionParameters = parameters
        }

        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
            this@PlayerStateImpl.playbackState = playbackState
        }

        override fun onPlayWhenReadyChanged(
                playWhenReady: Boolean,
                @Player.PlayWhenReadyChangeReason reason: Int
        ) {
            this@PlayerStateImpl.playWhenReady = playWhenReady
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
            this@PlayerStateImpl.playbackSuppressionReason = playbackSuppressionReason
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            this@PlayerStateImpl.isPlaying = isPlaying
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            this@PlayerStateImpl.repeatMode = repeatMode
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            this@PlayerStateImpl.shuffleModeEnabled = shuffleModeEnabled
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            this@PlayerStateImpl.playerError = error
        }

        override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
        ) {
            this@PlayerStateImpl.mediaItemIndex = player.currentMediaItemIndex
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            this@PlayerStateImpl.playbackParameters = playbackParameters
        }

        override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
            this@PlayerStateImpl.seekBackIncrement = seekBackIncrementMs
        }

        override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
            this@PlayerStateImpl.seekForwardIncrement = seekForwardIncrementMs
        }

        override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
            this@PlayerStateImpl.maxSeekToPreviousPosition = maxSeekToPreviousPositionMs
        }

        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            //
        }

        override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
            this@PlayerStateImpl.audioAttributes = audioAttributes
        }

        override fun onVolumeChanged(volume: Float) {
            this@PlayerStateImpl.volume = volume
        }

        override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
            //
        }

        override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
            this@PlayerStateImpl.deviceInfo = deviceInfo
        }

        override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
            this@PlayerStateImpl.deviceVolume = volume
            this@PlayerStateImpl.isDeviceMuted = muted
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            this@PlayerStateImpl.videoSize = videoSize
        }

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            //
        }

        override fun onCues(cues: List<Cue>) {
            this@PlayerStateImpl.cues = cues
        }


    }

    init {
        player.addListener(listener)
    }

    override fun dispose() {
        player.removeListener(listener)
    }
}
