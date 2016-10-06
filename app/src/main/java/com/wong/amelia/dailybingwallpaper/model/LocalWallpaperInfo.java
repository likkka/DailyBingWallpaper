package com.wong.amelia.dailybingwallpaper.model;

import android.graphics.Bitmap;

/**
 * Created by amelia on 5/10/16.
 */
public class LocalWallpaperInfo {
    Bitmap wallpaper = null;
    String describption = null;
    String date = null;
    boolean inUse = false;

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    String url = null;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescribption() {
        return describption;
    }

    public void setDescribption(String describption) {
        this.describption = describption;
    }

    public Bitmap getWallpaper() {
        return wallpaper;
    }

    public void setWallpaper(Bitmap wallpaper) {
        this.wallpaper = wallpaper;
    }
}
