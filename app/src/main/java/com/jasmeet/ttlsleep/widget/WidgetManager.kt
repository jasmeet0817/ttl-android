package com.jasmeet.ttlsleep.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.jasmeet.ttlsleep.R

class WidgetManager {
    companion object {
        @JvmStatic
        fun updateWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TTLWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.ttl_widget_id)
            TTLWidgetProvider().onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}