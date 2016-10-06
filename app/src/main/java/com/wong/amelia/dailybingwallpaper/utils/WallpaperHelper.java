package com.wong.amelia.dailybingwallpaper.utils;

import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

import com.wong.amelia.dailybingwallpaper.MainActivity;
import com.wong.amelia.dailybingwallpaper.R;
import com.wong.amelia.dailybingwallpaper.model.LocalWallpaperInfo;
import com.wong.amelia.dailybingwallpaper.widget.DetailWidgetProvider;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by amelia on 6/10/16.
 */
public class WallpaperHelper {
    public static void setWallpaper(Bitmap bitmap, Context context) {
        if (context == null) {
            Log.e(MainActivity.TAG, "setWP: context = null.");
            return;
        }
        WeakReference<Context> contextRefer = new WeakReference<>(context);
        if (bitmap == null) {
            MainActivity.makeToast(contextRefer.get(), "bitmap == null");
            return;
        }
        WallpaperManager wallpaperManager = (WallpaperManager)((contextRefer.get()).getSystemService(Context.WALLPAPER_SERVICE));
        try {
            wallpaperManager.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //更新桌面插件信息
    public static void updateWidget(Context context, LocalWallpaperInfo info) {
        if (context == null) {
            Log.e(MainActivity.TAG, "saveWP: context = null.");
            return;
        }
        WeakReference<Context> contextRefer = new WeakReference<>(context);
        if (context == null) {
            Log.e(MainActivity.TAG, "widget update: context = null.");
            return;
        }
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        String date = info.getDate();
        String pat1 = "yyyyMMdd";//date:20161005 --> 2016/10/05
        String pat2 = "yyyy/MM/dd";
        SimpleDateFormat format1 = new SimpleDateFormat(pat1);
        SimpleDateFormat format2 = new SimpleDateFormat(pat2);
        try {
            Date d = format1.parse(date);
            date = format2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String detail = date + "\n" + info.getDescribption();
        remoteViews.setTextViewText(R.id.widget_detail, detail);

        Intent intent = new Intent();
        intent.setAction(DetailWidgetProvider.REFRESH_ACTION);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_refresh, pending);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(contextRefer.get());
        ComponentName componentName = new ComponentName(contextRefer.get(), DetailWidgetProvider.class);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }
}
