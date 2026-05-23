package com.arturo254.opentune.widget

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.arturo254.opentune.playback.MusicService
import com.arturo254.opentune.utils.reportException

class WidgetBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                val controller = try {
                    controllerFuture.get()
                } catch (e: Exception) {
                    null
                }
                if (controller == null) return@addListener

                try {
                    when (intent.action) {
                        MusicWidgetProvider.ACTION_WIDGET_PLAY_PAUSE -> {
                            handlePlayPause(controller)
                        }
                        MusicWidgetProvider.ACTION_WIDGET_NEXT -> {
                            handleNext(controller)
                        }
                        MusicWidgetProvider.ACTION_WIDGET_PREVIOUS -> {
                            handlePrevious(controller)
                        }
                    }
                } finally {
                    controller.release()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun handlePlayPause(controller: MediaController) {
        when {
            controller.playWhenReady -> controller.pause()
            controller.playbackState == Player.STATE_ENDED -> {
                controller.seekTo(0, 0)
                controller.play()
            }
            else -> controller.play()
        }
    }

    private fun handleNext(controller: MediaController) {
        controller.seekToNext()
    }

    private fun handlePrevious(controller: MediaController) {
        controller.seekToPrevious()
    }
}