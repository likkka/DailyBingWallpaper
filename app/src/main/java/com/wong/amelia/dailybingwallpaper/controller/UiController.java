package com.wong.amelia.dailybingwallpaper.controller;

import android.graphics.Bitmap;

import com.wong.amelia.dailybingwallpaper.model.LocalWallpaperInfo;

/**
 * Created by amelia on 6/10/16.
 */
public interface UiController {
    void showOrHideProgressBar(boolean show);
    void refreshWidgetUi(LocalWallpaperInfo info);
    void refreshMainUi(LocalWallpaperInfo info);
    void setSystemWallpaper(Bitmap bitmap);
}
