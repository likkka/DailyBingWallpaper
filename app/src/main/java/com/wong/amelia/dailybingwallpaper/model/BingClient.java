package com.wong.amelia.dailybingwallpaper.model;

import android.graphics.Canvas;

import com.squareup.okhttp.ResponseBody;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.Url;
import rx.Observable;

/**
 * Created by amelia on 3/10/16.
 */
public interface BingClient {
//    http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1

    @GET("HPImageArchive.aspx")
    Call<BingWallpaperInfos> fetchBingWallpaperInfos(@Query("format") String format, @Query("idx") int idx, @Query("n") int n);

    @GET
    Call<ResponseBody> getDailyBingWallpaperResponseBody(@Url String url);
}
