package com.md.rsswidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int noOfWidgets = appWidgetIds.length;

        for(int i =0;i<noOfWidgets;i++)
        {
            int appWidgetId = appWidgetIds[i];
            Intent intent = new Intent(context,NewsService.class);
            intent.putExtra("appWidgetId",appWidgetId);
            context.startService(intent);


        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

