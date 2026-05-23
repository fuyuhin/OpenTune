package com.arturo254.opentune.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.arturo254.opentune.R
import com.arturo254.opentune.models.MediaMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object MusicWidgetUpdater {

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun updateWidget(
        context: Context,
        mediaMetadata: MediaMetadata?,
        isPlaying: Boolean,
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, MusicWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) return

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.music_widget)

            MusicWidgetProvider.applyDefaultClickIntents(context, views)

            if (mediaMetadata != null) {
                views.setTextViewText(R.id.widget_title, mediaMetadata.title)
                val artistText = mediaMetadata.artists.joinToString(", ") { it.name }
                views.setTextViewText(R.id.widget_artist, artistText)
                views.setImageViewResource(
                    R.id.widget_play_pause,
                    if (isPlaying) R.drawable.pause else R.drawable.play
                )
            } else {
                views.setTextViewText(
                    R.id.widget_title,
                    context.getString(R.string.widget_not_playing)
                )
                views.setTextViewText(R.id.widget_artist, "")
                views.setImageViewResource(R.id.widget_play_pause, R.drawable.play)
                views.setImageViewResource(R.id.widget_artwork, R.mipmap.ic_launcher)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        mediaMetadata?.thumbnailUrl?.let { url ->
            loadArtwork(context, url, appWidgetManager, appWidgetIds)
        }
    }

    private fun loadArtwork(
        context: Context,
        url: String,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        widgetScope.launch(Dispatchers.IO) {
            try {
                val loader = ImageLoader.Builder(context).build()
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(256)
                    .allowHardware(false)
                    .build()
                val result = loader.execute(request)
                val bitmap = result.image?.toBitmap() ?: return@launch

                launch(Dispatchers.Main) {
                    for (appWidgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.music_widget)
                        views.setImageViewBitmap(R.id.widget_artwork, bitmap)
                        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}