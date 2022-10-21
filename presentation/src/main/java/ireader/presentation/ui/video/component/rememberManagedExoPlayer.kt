package ireader.presentation.ui.video.component

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import org.koin.core.annotation.Factory


@Composable
fun rememberManagedExoPlayer(): State<Player?> = rememberManagedPlayer { context ->
    val builder = ExoPlayer.Builder(context)
    builder.setMediaSourceFactory(ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context)))
    builder.build().apply {
        playWhenReady = true
    }
}


@Factory
class PlayerCreator(private val context: Context) {
    fun init(): Player {
        val builder = ExoPlayer.Builder(context)
        builder.setMediaSourceFactory(ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context)))
        return builder.build().apply {
            playWhenReady = true
        }

    }
}

