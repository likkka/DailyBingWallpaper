package com.wong.amelia.dailybingwallpaper.controller;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;

import com.squareup.okhttp.ResponseBody;
import com.wong.amelia.dailybingwallpaper.model.BingClient;
import com.wong.amelia.dailybingwallpaper.model.BingWallpaperInfos;
import com.wong.amelia.dailybingwallpaper.model.LocalWallpaperInfo;
import com.wong.amelia.dailybingwallpaper.model.ServiceGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import retrofit.Call;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import com.wong.amelia.dailybingwallpaper.MainActivity;
/**
 * Created by amelia on 5/10/16.
 */
public class RxController {
    WeakReference<UiController> uiControllerWeakRefer;
    WeakReference<Context> contextWeakRefer;

    public RxController(Context context, UiController controller){
        uiControllerWeakRefer = new WeakReference<>(controller);
        contextWeakRefer = new WeakReference<>(context);
    }

    /**
     * bing返回结果：
     * {"images":[{"startdate":"20161002","fullstartdate":"201610021600","enddate":"20161003","url":"http://s.cn.bing.net/az/hprichbg/rb/CliffDwelling_ZH-CN11875663989_1920x1080.jpg","urlbase":"/az/hprichbg/rb/CliffDwelling_ZH-CN11875663989","copyright":"绮丽峡谷国家遗迹的悬崖屋，美国亚利桑那州 (© Design Pics/Offset)","copyrightlink":"http://www.bing.com/search?q=%E5%8D%B0%E7%AC%AC%E5%AE%89%E9%81%97%E5%9D%80&form=hpcapt&mkt=zh-cn","wp":true,"hsh":"d6ab145b56648afcb6276c132cb44343","drk":1,"top":1,"bot":1,"hs":[]}],"tooltips":{"loading":"正在加载...","previous":"上一个图像","next":"下一个图像","walle":"此图片不能下载用作壁纸。","walls":"下载今日美图。仅限用作桌面壁纸。"}}
     * @param infos
     * @return
     */
    public synchronized String getDailyWallpaperUrl(BingWallpaperInfos infos) {
        return infos.images.get(0).url.replace("_1920x1080", "_1080x1920");
    }

    public synchronized String getDailyWallpaperDate(BingWallpaperInfos infos) {
        return infos.images.get(0).startdate;
    }

    public synchronized String getDailyWallpaperDescribption(BingWallpaperInfos infos) {
        return infos.images.get(0).copyright;
    }

    Func1<Call<BingWallpaperInfos>, LocalWallpaperInfo> infos2strF = new Func1<Call<BingWallpaperInfos>, LocalWallpaperInfo>() {
        @Override
        public LocalWallpaperInfo call(Call<BingWallpaperInfos> bingWallpaperInfosCall) {
            Log.i(MainActivity.TAG, "func info -> string: " + Thread.currentThread().getName());
            LocalWallpaperInfo result = null;
            try {
                BingWallpaperInfos infos = bingWallpaperInfosCall.execute().body();
                result = new LocalWallpaperInfo();
                result.setDate(getDailyWallpaperDate(infos));
                result.setDescribption(getDailyWallpaperDescribption(infos));
                result.setUrl(getDailyWallpaperUrl(infos));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    };

    Func1<LocalWallpaperInfo, LocalWallpaperInfo> str2BpF = new Func1<LocalWallpaperInfo, LocalWallpaperInfo>() {
        @Override
        public LocalWallpaperInfo call(LocalWallpaperInfo info) {
            Log.i(MainActivity.TAG, "func string -> bitmap: " + Thread.currentThread().getName());
            BingClient client = ServiceGenerator.createService(BingClient.class);
            Call<ResponseBody> response = client.getDailyBingWallpaperResponseBody(info.getUrl());
            try {
                Bitmap wallpaper = BitmapFactory.decodeStream(response.execute().body().byteStream());
                info.setWallpaper(wallpaper);
                return info;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    /**
     * 将传入的图片设为桌面壁纸
     */
    private Func1<LocalWallpaperInfo, LocalWallpaperInfo> setWallpaperF = new Func1<LocalWallpaperInfo, LocalWallpaperInfo>() {
        @Override
        public LocalWallpaperInfo call(LocalWallpaperInfo info) {
            if (uiControllerWeakRefer.get() != null) {
                uiControllerWeakRefer.get().setSystemWallpaper(info.getWallpaper());
            }
            return info;
        }
    };

    /**
     * 将传入的图片保存到硬盘
     */
    private Func1<LocalWallpaperInfo, LocalWallpaperInfo> save2diskF = new Func1<LocalWallpaperInfo, LocalWallpaperInfo>() {
        @Override
        public LocalWallpaperInfo call(LocalWallpaperInfo info) {
            UiController controller = uiControllerWeakRefer.get();
            Context context = contextWeakRefer.get();
            if (controller == null || context == null) {
                Log.e(MainActivity.TAG, "save2diskF: context or controller = null.");
                return info;
            }
            File f = new File(context.getExternalCacheDir(), info.getDate() + ".png");
            if (f.exists()) {
                if (MainActivity.DBG) {
                    Log.i(MainActivity.TAG, info.getDate() + " Wallpaper already exists in disk!");
                }
                return info;
            }
            try {
                FileOutputStream out = new FileOutputStream(f);
                info.getWallpaper().compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (Throwable ignore) {
                Log.e(MainActivity.TAG, "save failed");
            }
            return info;
        }
    };

    Action0 showProgressAction = new Action0() {
        @Override
        public void call() {
            if (uiControllerWeakRefer.get() != null) {
                uiControllerWeakRefer.get().showOrHideProgressBar(true);
            }
        }
    };


    Subscriber<LocalWallpaperInfo> uiRefreshSubscriber = new Subscriber<LocalWallpaperInfo>() {
        @Override
        public void onCompleted() {
            UiController controller = uiControllerWeakRefer.get();
            if (controller != null) {
                uiControllerWeakRefer.get().showOrHideProgressBar(false);
            }
        }

        @Override
        public void onError(Throwable e) {
            if (uiControllerWeakRefer.get() == null) {
                Log.e(MainActivity.TAG, "controller == null.");
            }
            e.printStackTrace();
        }

        @Override
        public void onNext(LocalWallpaperInfo info) {
            if (uiControllerWeakRefer.get() != null) {
                uiControllerWeakRefer.get().refreshMainUi(info);
                uiControllerWeakRefer.get().refreshWidgetUi(info);
            }
        }
    };

    public void loadNewWallpaper() {
        BingClient client = ServiceGenerator.createService(BingClient.class);
        rx.Observable
                .just(client.fetchBingWallpaperInfos("js", 0, 1))
                .doOnSubscribe(showProgressAction)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(infos2strF)
                .map(str2BpF)
                .map(setWallpaperF)
                .map(save2diskF)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uiRefreshSubscriber);
    }

}
