package com.arturo254.opentune.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.arturo254.opentune.MainActivity
import com.arturo254.opentune.R

class MusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.music_widget)
            applyDefaultClickIntents(context, views)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        const val ACTION_WIDGET_PLAY_PAUSE = "com.arturo254.opentune.widget.PLAY_PAUSE"
        const val ACTION_WIDGET_NEXT = "com.arturo254.opentune.widget.NEXT"
        const val ACTION_WIDGET_PREVIOUS = "com.arturo254.opentune.widget.PREVIOUS"
        const val ACTION_WIDGET_OPEN_APP = "com.arturo254.opentune.widget.OPEN_APP"

        fun applyDefaultClickIntents(context: Context, views: RemoteViews) {
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_WIDGET_OPEN_APP
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_artwork, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_title, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_artist, openAppPendingIntent)

            val playPauseIntent = Intent(context, WidgetBroadcastReceiver::class.java).apply {
                action = ACTION_WIDGET_PLAY_PAUSE
            }
            views.setOnClickPendingIntent(
                R.id.widget_play_pause,
                PendingIntent.getBroadcast(
                    context, 1, playPauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            val nextIntent = Intent(context, WidgetBroadcastReceiver::class.java).apply {
                action = ACTION_WIDGET_NEXT
            }
            views.setOnClickPendingIntent(
                R.id.widget_next,
                PendingIntent.getBroadcast(
                    context, 2, nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            val previousIntent = Intent(context, WidgetBroadcastReceiver::class.java).apply {
                action = ACTION_WIDGET_PREVIOUS
            }
            views.setOnClickPendingIntent(
                R.id.widget_previous,
                PendingIntent.getBroadcast(
                    context, 3, previousIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }
}