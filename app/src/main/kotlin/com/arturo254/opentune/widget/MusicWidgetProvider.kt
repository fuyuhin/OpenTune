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
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
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

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        try {
            context.startService(
                Intent(context, MusicService::class.java).apply {
                    action = ACTION_UPDATE_WIDGET
                }
            )
        } catch (_: Exception) { }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.arturo254.opentune.widget.UPDATE"
        const val EXTRA_TITLE = "widget_title"
        const val EXTRA_ARTIST = "widget_artist"
        const val EXTRA_ALBUM = "widget_album"
        const val EXTRA_IS_PLAYING = "widget_is_playing"
        const val EXTRA_ART_URL = "widget_art_url"
        const val EXTRA_IS_LIKED = "widget_is_liked"
        const val EXTRA_IS_SHUFFLE_ON = "widget_is_shuffle_on"
        const val EXTRA_LYRICS_LINE = "widget_lyrics_line"

        const val ACTION_WIDGET_PLAY_PAUSE = "com.arturo254.opentune.widget.cmd.PLAY_PAUSE"
        const val ACTION_WIDGET_NEXT = "com.arturo254.opentune.widget.cmd.NEXT"
        const val ACTION_WIDGET_PREV = "com.arturo254.opentune.widget.cmd.PREV"
        const val ACTION_WIDGET_LIKE = "com.arturo254.opentune.widget.cmd.LIKE"
        const val ACTION_WIDGET_SHUFFLE = "com.arturo254.opentune.widget.cmd.SHUFFLE"

        private const val LYRIC_PREFIX = "♪ "

        // Buttons dimmed when there is no track loaded (per spec).
        private const val NO_TRACK_BUTTON_ALPHA = 0.5f
        private const val FULL_BUTTON_ALPHA     = 1.0f

        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = buildViews(
                context, manager, widgetId,
                title = null, artist = null, album = null,
                isPlaying = false, isLiked = false,
                isShuffleOn = false, lyricsLine = null,
                artBitmap = null,
            )
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
            isShuffleOn: Boolean,
            lyricsLine: String? = null,
        ) {
            // First push with transparent art placeholder.
            // NEVER call set*() on the returned RemoteViews — on API 31+ it is a composite
            // (sizeMap) view and Android forbids modifying it after construction.
            val views = buildViews(
                context, manager, widgetId,
                title, artist, album, isPlaying, isLiked, isShuffleOn, lyricsLine,
                artBitmap = null,
            )
            manager.updateAppWidget(widgetId, views)

            if (!artUrl.isNullOrBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = loadAndRoundBitmap(artUrl)
                    withContext(Dispatchers.Main) {
                        // Rebuild entirely; bitmap baked into each individual RemoteViews
                        // *before* the composite is assembled.
                        val viewsWithArt = buildViews(
                            context, manager, widgetId,
                            title, artist, album, isPlaying, isLiked, isShuffleOn, lyricsLine,
                            artBitmap = bitmap,
                        )
                        manager.updateAppWidget(widgetId, viewsWithArt)
                    }
                }
            }
        }

        /**
         * Builds a fully configured [RemoteViews] for the widget host.
         * On API 31+ returns a responsive [RemoteViews] that automatically switches
         * between the 4×2 and 4×1 layouts. On older APIs returns a single layout.
         *
         * Every set*() call is made on individual layout views BEFORE they are
         * wrapped into a composite; Android throws if you mutate a composite.
         */
        private fun buildViews(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            title: String?,
            artist: String?,
            album: String?,
            isPlaying: Boolean,
            isLiked: Boolean,
            isShuffleOn: Boolean,
            lyricsLine: String?,
            artBitmap: Bitmap?,
        ): RemoteViews {
            val options = manager.getAppWidgetOptions(widgetId)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                buildResponsiveViews(context, options, title, artist, album, isPlaying, isLiked, isShuffleOn, lyricsLine, artBitmap)
            } else {
                val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 130)
                val isCompact = heightDp < 100
                val layoutId = if (isCompact) R.layout.widget_music_player_small else R.layout.widget_music_player
                val views = RemoteViews(context.packageName, layoutId)
                populateViews(
                    context, views, title,
                    if (isCompact) null else artist,
                    if (isCompact) null else album,
                    isPlaying, isLiked, isShuffleOn,
                    if (isCompact) null else lyricsLine,
                )
                applyArt(views, artBitmap)
                setClickListeners(context, views)
                views
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun buildResponsiveViews(
            context: Context,
            options: Bundle,
            title: String?,
            artist: String?,
            album: String?,
            isPlaying: Boolean,
            isLiked: Boolean,
            isShuffleOn: Boolean,
            lyricsLine: String?,
            artBitmap: Bitmap?,
        ): RemoteViews {
            val fullViews  = RemoteViews(context.packageName, R.layout.widget_music_player)
            val smallViews = RemoteViews(context.packageName, R.layout.widget_music_player_small)

            populateViews(context, fullViews,  title, artist, album, isPlaying, isLiked, isShuffleOn, lyricsLine)
            populateViews(context, smallViews, title, null,   null,  isPlaying, isLiked, isShuffleOn, null)
            applyArt(fullViews,  artBitmap)
            applyArt(smallViews, artBitmap)
            setClickListeners(context, fullViews)
            setClickListeners(context, smallViews)

            // Dynamic square art: width = current widget height (per orientation).
            @Suppress("DEPRECATION")
            val sizes = options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
            val minH  = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 130)
            val fullH  = sizes?.maxByOrNull { it.height }?.height?.toInt()?.coerceAtLeast(80) ?: minH
            val smallH = sizes?.minByOrNull { it.height }?.height?.toInt()?.coerceAtLeast(40)
                ?: (minH / 2).coerceAtLeast(56)

            fullViews.setViewLayoutWidth(R.id.widget_album_art,  fullH.toFloat(),  TypedValue.COMPLEX_UNIT_DIP)
            smallViews.setViewLayoutWidth(R.id.widget_album_art, smallH.toFloat(), TypedValue.COMPLEX_UNIT_DIP)

            return RemoteViews(
                mapOf(
                    SizeF(250f, 110f) to fullViews,
                    SizeF(250f,  56f) to smallViews,
                )
            )
        }

        private fun applyArt(views: RemoteViews, bitmap: Bitmap?) {
            if (bitmap != null) {
                views.setImageViewBitmap(R.id.widget_album_art, bitmap)
            } else {
                views.setImageViewResource(R.id.widget_album_art, android.R.color.transparent)
            }
        }

        private fun populateViews(
            context: Context,
            views: RemoteViews,
            title: String?,
            artist: String?,
            album: String?,
            isPlaying: Boolean,
            isLiked: Boolean,
            isShuffleOn: Boolean,
            lyricsLine: String?,
        ) {
            val hasTrack = !title.isNullOrBlank()

            views.setTextViewText(
                R.id.widget_song_title,
                title?.ifBlank { null } ?: context.getString(R.string.not_playing),
            )

            val hasArtist = !artist.isNullOrBlank()
            views.setTextViewText(R.id.widget_artist_name, artist.orEmpty())
            views.setViewVisibility(R.id.widget_artist_name, if (hasArtist) View.VISIBLE else View.GONE)

            val hasAlbum = !album.isNullOrBlank()
            views.setTextViewText(R.id.widget_album_name, album.orEmpty())
            views.setViewVisibility(R.id.widget_album_name, if (hasAlbum) View.VISIBLE else View.GONE)

            val hasLyrics = !lyricsLine.isNullOrBlank()
            views.setTextViewText(
                R.id.widget_lyrics,
                if (hasLyrics) "$LYRIC_PREFIX${lyricsLine}" else "",
            )
            views.setViewVisibility(R.id.widget_lyrics, if (hasLyrics) View.VISIBLE else View.GONE)

            val playPauseIcon = if (isPlaying) R.drawable.ic_pause_white else R.drawable.ic_play_white
            views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)

            applyButtonStates(views, isLiked, isShuffleOn, hasTrack)
        }

        private fun applyButtonStates(views: RemoteViews, isLiked: Boolean, isShuffleOn: Boolean, hasTrack: Boolean) {
            // Pure-white tint on every control, no accent colours.
            views.setInt(R.id.widget_btn_prev,       "setColorFilter", Color.WHITE)
            views.setInt(R.id.widget_btn_next,       "setColorFilter", Color.WHITE)
            views.setInt(R.id.widget_btn_play_pause, "setColorFilter", Color.WHITE)
            views.setInt(R.id.widget_btn_like,       "setColorFilter", Color.WHITE)
            views.setInt(R.id.widget_btn_shuffle,    "setColorFilter", Color.WHITE)

            // Liked: filled heart (still white). Per spec, no accent.
            views.setImageViewResource(
                R.id.widget_btn_like,
                if (isLiked) R.drawable.favorite else R.drawable.favorite_border,
            )

            // Shuffle: subtly indicate "on" via the dedicated icon variant.
            views.setImageViewResource(
                R.id.widget_btn_shuffle,
                if (isShuffleOn) R.drawable.shuffle_on else R.drawable.shuffle,
            )

            // No-track state: dim all controls to 50% per spec.
            val alpha = if (hasTrack) FULL_BUTTON_ALPHA else NO_TRACK_BUTTON_ALPHA
            views.setFloat(R.id.widget_btn_like,       "setAlpha", alpha)
            views.setFloat(R.id.widget_btn_prev,       "setAlpha", alpha)
            views.setFloat(R.id.widget_btn_play_pause, "setAlpha", alpha)
            views.setFloat(R.id.widget_btn_next,       "setAlpha", alpha)
            views.setFloat(R.id.widget_btn_shuffle,    "setAlpha", alpha)
        }

        private fun setClickListeners(context: Context, views: RemoteViews) {
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, buildServiceIntent(context, ACTION_WIDGET_PLAY_PAUSE))
            views.setOnClickPendingIntent(R.id.widget_btn_next,       buildServiceIntent(context, ACTION_WIDGET_NEXT))
            views.setOnClickPendingIntent(R.id.widget_btn_prev,       buildServiceIntent(context, ACTION_WIDGET_PREV))
            views.setOnClickPendingIntent(R.id.widget_btn_like,       buildServiceIntent(context, ACTION_WIDGET_LIKE))
            views.setOnClickPendingIntent(R.id.widget_btn_shuffle,    buildServiceIntent(context, ACTION_WIDGET_SHUFFLE))
            val openApp = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_song_title,  openApp)
            views.setOnClickPendingIntent(R.id.widget_artist_name, openApp)
            views.setOnClickPendingIntent(R.id.widget_album_name,  openApp)
            views.setOnClickPendingIntent(R.id.widget_album_art,   openApp)
        }

        private fun buildServiceIntent(context: Context, action: String): PendingIntent =
            PendingIntent.getService(
                context, action.hashCode(),
                Intent(action, null, context, MusicService::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun loadAndRoundBitmap(url: String): Bitmap? = try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.connect()
            val stream: InputStream = conn.inputStream
            BitmapFactory.decodeStream(stream).also { conn.disconnect() }?.let { makeSquareRounded(it) }
        } catch (_: Exception) { null }

        /**
         * Center-crops to a square and rounds corners at roughly 10% of the bitmap pixel
         * size — at the full widget art display size this works out to ~14dp,
         * matching the surface stroke radius.
         */
        private fun makeSquareRounded(bitmap: Bitmap): Bitmap {
            val size = minOf(bitmap.width, bitmap.height)
            val x = (bitmap.width - size) / 2
            val y = (bitmap.height - size) / 2
            val square = if (x == 0 && y == 0) bitmap
                         else Bitmap.createBitmap(bitmap, x, y, size, size)
            val radius = size * 0.10f
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
