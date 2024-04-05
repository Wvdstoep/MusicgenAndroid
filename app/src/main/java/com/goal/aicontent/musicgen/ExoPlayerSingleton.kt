package com.goal.aicontent.musicgen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

@UnstableApi
object ExoPlayerSingleton {
    private var exoPlayer: ExoPlayer? = null
    var playerStateCallback: PlayerStateCallback? = null
    fun preview(context: Context, mediaUri: Uri, startMs: Long, endMs: Long) {
        val player = getExoPlayer(context)

        // Create and prepare the media item
        val mediaItem = MediaItem.fromUri(mediaUri)
        player.setMediaItem(mediaItem)
        player.prepare()

        // Seek to the starting position and start playback
        player.seekTo(startMs)
        player.playWhenReady = true

        // Handler to periodically check playback position
        val handler = Handler(Looper.getMainLooper())
        val checkPositionRunnable = object : Runnable {
            override fun run() {
                if (player.currentPosition >= endMs) {
                    // Stop playback when reaching the end time
                    player.pause()
                    player.stop()
                    player.clearMediaItems()
                } else {
                    // Re-post the runnable to keep checking periodically
                    handler.postDelayed(this, 1000) // Check every second
                }
            }
        }

        // Start the periodic check
        handler.post(checkPositionRunnable)

        // Make sure to remove callbacks when the player is released or no longer needed
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                    handler.removeCallbacks(checkPositionRunnable)
                }
            }
        })
    }
    fun isPlaying(): Boolean {
        // Returns true if ExoPlayer is playing.
        return exoPlayer?.isPlaying ?: false
    }
    fun getExoPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            val renderersFactory = DefaultRenderersFactory(context)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            val dataSourceFactory: DataSource.Factory =
                DefaultDataSource.Factory(context)
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(2500, 30000, 1000, 2000)
                .build()

            exoPlayer = ExoPlayer.Builder(context, renderersFactory)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(mediaSourceFactory)
                .build().apply {
                    addListener(object : Player.Listener {
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            super.onMediaItemTransition(mediaItem, reason)
                            Log.d("ExoPlayerSingleton", "Media item transition: $mediaItem")
                            playerStateCallback?.onPlaybackStateChanged(isPlaying)
                        }

                        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                            super.onPlayWhenReadyChanged(playWhenReady, reason)
                            Log.d("ExoPlayerSingleton", if (playWhenReady) "Playback started" else "Playback paused")
                        }
                    })
                }

            (context as? LifecycleOwner)?.lifecycle?.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        Log.d("ExoPlayerSingleton", "Lifecycle ON_DESTROY: Releasing ExoPlayer")
                        releaseExoPlayer()
                    }
                }
            })
        } else {
            Log.d("ExoPlayerSingleton", "Using existing ExoPlayer instance")
        }
        return exoPlayer!!
    }

    fun releaseExoPlayer() {
        Log.d("ExoPlayerSingleton", "Releasing ExoPlayer")
        exoPlayer?.release()
        exoPlayer = null
    }

    interface PlayerStateCallback {
        fun onPlaybackStateChanged(isPlaying: Boolean)
    }
}
