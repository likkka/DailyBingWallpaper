package com.wong.amelia.dailybingwallpaper.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.wong.amelia.dailybingwallpaper.MainActivity;
import com.wong.amelia.dailybingwallpaper.R;
import com.wong.amelia.dailybingwallpaper.controller.RxController;
import com.wong.amelia.dailybingwallpaper.controller.UiController;
import com.wong.amelia.dailybingwallpaper.model.LocalWallpaperInfo;
import com.wong.amelia.dailybingwallpaper.utils.WallpaperHelper;

import java.lang.ref.WeakReference;

/**
 * Created by amelia on 5/10/16.
 */
public class DetailWidgetProvider extends AppWidgetProvider implements UiController {
    WeakReference<Context> contextWeakRefer;

    public static final String REFRESH_ACTION = "com.rio.refresh";
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        refreshOnUpdate(context);
    }

    private void refreshOnUpdate(Context context) {
        if (contextWeakRefer == null || contextWeakRefer.get() == null) {
            contextWeakRefer = new WeakReference<>(context);
        }
        if (WallpaperHelper.isNetworkConnected(context)) {
            RxController controller = new RxController(context, this);
            controller.loadNewWallpaper();
            MainActivity.makeToast(context, "正在更新bing壁纸");
        } else {
            LocalWallpaperInfo defaultInfo = new LocalWallpaperInfo();
            defaultInfo.setDate("00000000");
            defaultInfo.setDescribption(context.getResources().getString(R.string.default_detail));
            refreshWidgetUi(defaultInfo);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (TextUtils.equals(intent.getAction(), REFRESH_ACTION)) {
            refreshOnUpdate(context);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void showOrHideProgressBar(boolean show) {
        //show nothing
    }

    @Override
    public void refreshWidgetUi(LocalWallpaperInfo info) {
        WallpaperHelper.updateWidget(contextWeakRefer.get(), info);
    }

    @Override
    public void refreshMainUi(LocalWallpaperInfo info) {

    }

    @Override
    public void setSystemWallpaper(Bitmap bitmap) {
        WallpaperHelper.setWallpaper(bitmap, contextWeakRefer.get());
    }
}
