package org.ireader.domain.services.tts_service.media_player
import android.media.session.PlaybackState
import android.support.v4.media.session.PlaybackStateCompat

/**
 * Useful extension methods for [PlaybackStateCompat].
 */
inline val PlaybackStateCompat.isPrepared
    get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
        (state == PlaybackStateCompat.STATE_PLAYING) ||
        (state == PlaybackStateCompat.STATE_PAUSED)

inline val PlaybackState.isPrepared
    get() = (state == PlaybackState.STATE_BUFFERING) ||
        (state == PlaybackState.STATE_PLAYING) ||
        (state == PlaybackState.STATE_PAUSED)

inline val PlaybackStateCompat.isPlaying
    get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
        (state == PlaybackStateCompat.STATE_PLAYING)

inline val PlaybackState.isPlaying
    get() = (state == PlaybackState.STATE_BUFFERING) ||
        (state == PlaybackState.STATE_PLAYING)

inline val PlaybackStateCompat.isPlayEnabled
    get() = (actions and PlaybackStateCompat.ACTION_PLAY != 0L) ||
        (
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L) &&
                (state == PlaybackStateCompat.STATE_PAUSED)
            )

inline val PlaybackState.isPlayEnabled
    get() = (actions and PlaybackState.ACTION_PLAY != 0L) ||
        (
            (actions and PlaybackState.ACTION_PLAY_PAUSE != 0L) &&
                (state == PlaybackState.STATE_PAUSED)
            )

inline val PlaybackStateCompat.isPauseEnabled
    get() = (actions and PlaybackStateCompat.ACTION_PAUSE != 0L) ||
        (
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L) &&
                (
                    state == PlaybackStateCompat.STATE_BUFFERING ||
                        state == PlaybackStateCompat.STATE_PLAYING
                    )
            )
inline val PlaybackState.isPauseEnabled
    get() = (actions and PlaybackState.ACTION_PAUSE != 0L) ||
        (
            (actions and PlaybackState.ACTION_PLAY_PAUSE != 0L) &&
                (
                    state == PlaybackState.STATE_BUFFERING ||
                        state == PlaybackState.STATE_PLAYING
                    )
            )

inline val PlaybackStateCompat.isSkipToNextEnabled
    get() = actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L
inline val PlaybackState.isSkipToNextEnabled
    get() = actions and PlaybackState.ACTION_SKIP_TO_NEXT != 0L

inline val PlaybackStateCompat.isSkipToPreviousEnabled
    get() = actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L

inline val PlaybackState.isSkipToPreviousEnabled
    get() = actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L

inline val PlaybackStateCompat.stateName
    get() = when (state) {
        PlaybackStateCompat.STATE_NONE -> "STATE_NONE"
        PlaybackStateCompat.STATE_STOPPED -> "STATE_STOPPED"
        PlaybackStateCompat.STATE_PAUSED -> "STATE_PAUSED"
        PlaybackStateCompat.STATE_PLAYING -> "STATE_PLAYING"
        PlaybackStateCompat.STATE_FAST_FORWARDING -> "STATE_FAST_FORWARDING"
        PlaybackStateCompat.STATE_REWINDING -> "STATE_REWINDING"
        PlaybackStateCompat.STATE_BUFFERING -> "STATE_BUFFERING"
        PlaybackStateCompat.STATE_ERROR -> "STATE_ERROR"
        else -> "UNKNOWN_STATE"
    }
inline val PlaybackState.stateName
    get() = when (state) {
        PlaybackState.STATE_NONE -> "STATE_NONE"
        PlaybackState.STATE_STOPPED -> "STATE_STOPPED"
        PlaybackState.STATE_PAUSED -> "STATE_PAUSED"
        PlaybackState.STATE_PLAYING -> "STATE_PLAYING"
        PlaybackState.STATE_FAST_FORWARDING -> "STATE_FAST_FORWARDING"
        PlaybackState.STATE_REWINDING -> "STATE_REWINDING"
        PlaybackState.STATE_BUFFERING -> "STATE_BUFFERING"
        PlaybackState.STATE_ERROR -> "STATE_ERROR"
        else -> "UNKNOWN_STATE"
    }
