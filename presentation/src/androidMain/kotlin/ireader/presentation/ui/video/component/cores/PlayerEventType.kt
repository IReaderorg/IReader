package ireader.presentation.ui.video.component.cores


interface Track {
    /**
     * Unique among the class, used to check which track is used.
     * VideoTrack and AudioTrack can have the same id
     **/
    val id: String?
    val label: String?
//    val isCurrentlyPlaying: Boolean
    val language: String?
}

data class VideoTrack(
    override val id: String?,
    override val label: String?,
//    override val isCurrentlyPlaying: Boolean,
    override val language: String?,
    val width: Int?,
    val height: Int?,
) : Track

data class AudioTrack(
    override val id: String?,
    override val label: String?,
//    override val isCurrentlyPlaying: Boolean,
    override val language: String?,
) : Track

data class CurrentTracks(
    val currentVideoTrack: VideoTrack?,
    val currentAudioTrack: AudioTrack?,
    val allVideoTracks: List<VideoTrack>,
    val allAudioTracks: List<AudioTrack>,
)
