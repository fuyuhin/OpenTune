package com.arturo254.opentune.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import com.arturo254.opentune.MainActivity
import com.arturo254.opentune.R
import com.arturo254.opentune.playback.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.arturo254.opentune.widget.UPDATE"
        const val EXTRA_TITLE = "widget_title"
        const val EXTRA_ARTIST = "widget_artist"
        const val EXTRA_IS_PLAYING = "widget_is_playing"
        const val EXTRA_ART_URL = "widget_art_url"

        // Actions handled by MusicService.onStartCommand
        const val ACTION_WIDGET_PLAY_PAUSE = "com.arturo254.opentune.widget.cmd.PLAY_PAUSE"
        const val ACTION_WIDGET_NEXT = "com.arturo254.opentune.widget.cmd.NEXT"
        const val ACTION_WIDGET_PREV = "com.arturo254.opentune.widget.cmd.PREV"

        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_music_player)
            views.setTextViewText(R.id.widget_song_title, context.getString(R.string.not_playing))
            setClickListeners(context, views)
            manager.updateAppWidget(widgetId, views)
        }

        fun updateWidgetContent(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            title: String?,
            artist: String?,
            isPlaying: Boolean,
            artUrl: String?,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_music_player)
            views.setTextViewText(
                R.id.widget_song_title,
                title?.ifBlank { null } ?: context.getString(R.string.not_playing),
            )
            views.setTextViewText(R.id.widget_artist_name, artist.orEmpty())
            val playPauseIcon = if (isPlaying) R.drawable.ic_pause_white else R.drawable.ic_play_white
            views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)
            setClickListeners(context, views)
            manager.updateAppWidget(widgetId, views)

            if (!artUrl.isNullOrBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = loadBitmap(artUrl)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            views.setImageViewBitmap(R.id.widget_album_art, bitmap)
                        } else {
                            views.setImageViewResource(R.id.widget_album_art, R.drawable.ic_music_placeholder)
                        }
                        manager.updateAppWidget(widgetId, views)
                    }
                }
            } else {
                views.setImageViewResource(R.id.widget_album_art, R.drawable.ic_music_placeholder)
                manager.updateAppWidget(widgetId, views)
            }
        }

        private fun setClickListeners(context: Context, views: RemoteViews) {
            views.setOnClickPendingIntent(
                R.id.widget_btn_play_pause,
                buildServiceIntent(context, ACTION_WIDGET_PLAY_PAUSE),
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                buildServiceIntent(context, ACTION_WIDGET_NEXT),
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_prev,
                buildServiceIntent(context, ACTION_WIDGET_PREV),
            )
            val openApp = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_song_title, openApp)
            views.setOnClickPendingIntent(R.id.widget_artist_name, openApp)
            views.setOnClickPendingIntent(R.id.widget_album_art, openApp)
        }

        private fun buildServiceIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(action, null, context, MusicService::class.java)
            return PendingIntent.getService(
                context,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun loadBitmap(url: String): Bitmap? = try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            val stream: InputStream = connection.inputStream
            BitmapFactory.decodeStream(stream).also { connection.disconnect() }
        } catch (_: Exception) {
            null
        }
    }
}
