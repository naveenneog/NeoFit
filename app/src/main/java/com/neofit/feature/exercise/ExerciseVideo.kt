package com.neofit.feature.exercise

import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.neofit.R
import com.neofit.feature.common.DishImage

/**
 * Plays a Sora-generated exercise demo (muted, looping) using a media3 PlayerView
 * with surface_type=texture_view and resize_mode=fit, so the clip keeps its true
 * aspect ratio (never stretched or cropped) and composes/captures cleanly. The
 * step image is drawn behind as a fallback (and shows through any letterbox).
 */
@OptIn(UnstableApi::class)
@Composable
fun ExerciseMedia(
    videoUrl: String?,
    imageRef: String?,
    label: String,
    modifier: Modifier = Modifier,
) {
    var failed by remember(videoUrl) { mutableStateOf(false) }
    Box(modifier) {
        DishImage(imageRef, label, modifier = Modifier.matchParentSize())

        if (!videoUrl.isNullOrBlank() && !failed) {
            val context = LocalContext.current
            val player = remember(videoUrl) {
                ExoPlayer.Builder(context).build().apply {
                    trackSelectionParameters = trackSelectionParameters.buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                        .build()
                    setMediaItem(MediaItem.fromUri(videoUrl))
                    repeatMode = Player.REPEAT_MODE_ALL
                    volume = 0f
                    playWhenReady = true
                    addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) { failed = true }
                    })
                    prepare()
                }
            }
            DisposableEffect(videoUrl) { onDispose { player.release() } }
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { ctx ->
                    (LayoutInflater.from(ctx).inflate(R.layout.exercise_player, null) as PlayerView)
                        .apply { this.player = player }
                },
                // Reattach the freshly-remembered player when the exercise (videoUrl)
                // changes, otherwise the PlayerView keeps the previous, released player
                // and the clip never advances to the next exercise.
                update = { it.player = player },
            )
        }
    }
}
