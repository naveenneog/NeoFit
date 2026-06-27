package com.neofit.feature.exercise

import android.view.TextureView
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
import androidx.media3.exoplayer.ExoPlayer
import com.neofit.feature.common.DishImage

/**
 * Plays a Sora-generated exercise demo (muted, looping) into a TextureView so it
 * composes cleanly over the fallback image. The step image is always drawn
 * behind, so a missing/failed video degrades gracefully. Audio is disabled
 * (the clips are silent and some emulators lack the 96 kHz AAC decoder).
 */
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
                factory = { ctx -> TextureView(ctx).also { player.setVideoTextureView(it) } },
            )
        }
    }
}
