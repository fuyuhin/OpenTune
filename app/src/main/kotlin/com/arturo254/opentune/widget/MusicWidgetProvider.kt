package com.arturo254.opentune.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.View
import android.widget.RemoteViews
import androidx.media3.common.Player
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
        const val EXTRA_ALBUM = "widget_album"
        const val EXTRA_IS_PLAYING = "widget_is_playing"
        const val EXTRA_ART_URL = "widget_art_url"
        const val EXTRA_IS_LIKED = "widget_is_liked"
        const val EXTRA_REPEAT_MODE = "widget_repeat_mode"
        const val EXTRA_LYRICS_LINE = "widget_lyrics_line"

        // Actions handled by MusicService.onStartCommand
        const val ACTION_WIDGET_PLAY_PAUSE = "com.arturo254.opentune.widget.cmd.PLAY_PAUSE"
        const val ACTION_WIDGET_NEXT = "com.arturo254.opentune.widget.cmd.NEXT"
        const val ACTION_WIDGET_PREV = "com.arturo254.opentune.widget.cmd.PREV"
        const val ACTION_WIDGET_LIKE = "com.arturo254.opentune.widget.cmd.LIKE"
        const val ACTION_WIDGET_REPEAT = "com.arturo254.opentune.widget.cmd.REPEAT"

        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_music_player)
            views.setTextViewText(R.id.widget_song_title, context.getString(R.string.not_playing))
            views.setTextViewText(R.id.widget_artist_name, "")
            views.setTextViewText(R.id.widget_album_name, "")
            views.setViewVisibility(R.id.widget_artist_name, View.GONE)
            views.setViewVisibility(R.id.widget_album_name, View.GONE)
            // Empty state: clear any stale image so the anime placeholder never shows
            views.setImageViewResource(R.id.widget_album_art, android.R.color.transparent)
            applyButtonStates(views, isLiked = false, repeatMode = Player.REPEAT_MODE_OFF)
            setClickListeners(context, views)
            manager.updateAppWidget(widgetId, views)
        }

        fun updateWidgetContent(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            title: String?,
            artist: String?,
            album: String?,
            isPlaying: Boolean,
            artUrl: String?,
            isLiked: Boolean,
            repeatMode: Int,
            lyricsLine: String? = null,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_music_player)
            views.setTextViewText(
                R.id.widget_song_title,
                title?.ifBlank { null } ?: context.getString(R.string.not_playing),
            )

            val hasArtist = !artist.isNullOrBlank()
            views.setTextViewText(R.id.widget_artist_name, artist.orEmpty())
            views.setViewVisibility(
                R.id.widget_artist_name,
                if (hasArtist) View.VISIBLE else View.GONE,
            )

            val hasAlbum = !album.isNullOrBlank()
            views.setTextViewText(R.id.widget_album_name, album.orEmpty())
            views.setViewVisibility(
                R.id.widget_album_name,
                if (hasAlbum) View.VISIBLE else View.GONE,
            )

            val playPauseIcon = if (isPlaying) R.drawable.ic_pause_white else R.drawable.ic_play_white
            views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)

            val hasLyrics = !lyricsLine.isNullOrBlank()
            views.setTextViewText(R.id.widget_lyrics, lyricsLine.orEmpty())
            views.setViewVisibility(R.id.widget_lyrics, if (hasLyrics) View.VISIBLE else View.GONE)

            applyButtonStates(views, isLiked, repeatMode)
            setClickListeners(context, views)
            manager.updateAppWidget(widgetId, views)

            if (!artUrl.isNullOrBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = loadAndRoundBitmap(artUrl)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            views.setImageViewBitmap(R.id.widget_album_art, bitmap)
                        } else {
                            views.setImageViewResource(R.id.widget_album_art, android.R.color.transparent)
                        }
                        manager.updateAppWidget(widgetId, views)
                    }
                }
            } else {
                views.setImageViewResource(R.id.widget_album_art, android.R.color.transparent)
                manager.updateAppWidget(widgetId, views)
            }
        }

        /**
         * Apply icon variant + white colour filter for every control.
         * Like / repeat convey their state via icon (border ↔ filled,
         * off / one / all) — no coloured tint.
         */
        private fun applyButtonStates(views: RemoteViews, isLiked: Boolean, repeatMode: Int) {
            views.setInt(R.id.widget_btn_prev, "setColorFilter", Color.WHITE)
            views.setInt(R.id.widget_btn_next, "setColorFilter", Color.WHITE)
            // Play sits on a white circle, so its icon must stay dark to be visible
            views.setInt(R.id.widget_btn_play_pause, "setColorFilter", Color.BLACK)

            val likeIcon = if (isLiked) R.drawable.favorite else R.drawable.favorite_border
            views.setImageViewResource(R.id.widget_btn_like, likeIcon)
            views.setInt(R.id.widget_btn_like, "setColorFilter", Color.WHITE)

            val repeatIcon = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                Player.REPEAT_MODE_ALL -> R.drawable.repeat_on
                else -> R.drawable.repeat
            }
            views.setImageViewResource(R.id.widget_btn_repeat, repeatIcon)
            views.setInt(R.id.widget_btn_repeat, "setColorFilter", Color.WHITE)
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
            views.setOnClickPendingIntent(
                R.id.widget_btn_like,
                buildServiceIntent(context, ACTION_WIDGET_LIKE),
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_repeat,
                buildServiceIntent(context, ACTION_WIDGET_REPEAT),
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
            views.setOnClickPendingIntent(R.id.widget_album_name, openApp)
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

        private fun loadAndRoundBitmap(url: String): Bitmap? = try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            val stream: InputStream = connection.inputStream
            val original = BitmapFactory.decodeStream(stream).also { connection.disconnect() }
            original?.let { makeSquareRounded(it) }
        } catch (_: Exception) {
            null
        }

        /**
         * Center-crops to a square and rounds corners with ~15% radius, matching
         * the widget's 16dp outer corner radius at the 110dp art size visually.
         */
        private fun makeSquareRounded(bitmap: Bitmap): Bitmap {
            val size = minOf(bitmap.width, bitmap.height)
            val x = (bitmap.width - size) / 2
            val y = (bitmap.height - size) / 2
            val square = if (x == 0 && y == 0) bitmap
                         else Bitmap.createBitmap(bitmap, x, y, size, size)

            val radius = size * 0.15f
            val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            canvas.drawRoundRect(RectF(0f, 0f, size.toFloat(), size.toFloat()), radius, radius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(square, 0f, 0f, paint)
            return output
        }
    }
}
