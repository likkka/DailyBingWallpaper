package com.wong.amelia.dailybingwallpaper;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;
import com.wong.amelia.dailybingwallpaper.model.BingClient;
import com.wong.amelia.dailybingwallpaper.model.BingWallpaperInfos;
import com.wong.amelia.dailybingwallpaper.model.LocalWallpaperInfo;
import com.wong.amelia.dailybingwallpaper.model.ServiceGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "RIO";
    FrameLayout layout;
    Button button;
    TextView detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViews();

    }

    private void setUpViews() {
        layout = (FrameLayout) findViewById(R.id.bg);
        detail = (TextView) findViewById(R.id.describe);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        BingClient client = ServiceGenerator.createService(BingClient.class);
        rx.Observable
                .just(client.fetchBingWallpaperInfos("js", 0, 1))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(infos2strF)
                .map(str2BpF)
                .map(setWallpaperF)
                .map(save2diskF)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(setBgAction);

    }

    public void makeToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
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
            Log.i(TAG, "func info -> string: " + Thread.currentThread().getName());
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
            Log.i(TAG, "func string -> bitmap: " + Thread.currentThread().getName());
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
            setSystemWallpaper(info.getWallpaper());
            return info;
        }
    };

    /**
     * 将传入的图片保存到硬盘
     */
    private Func1<LocalWallpaperInfo, LocalWallpaperInfo> save2diskF = new Func1<LocalWallpaperInfo, LocalWallpaperInfo>() {
        @Override
        public LocalWallpaperInfo call(LocalWallpaperInfo info) {
//            File f = getExternalCacheDir();
            File f = new File(getExternalCacheDir(), info.getDate() + ".png");
            try {
                FileOutputStream out = new FileOutputStream(f);
                info.getWallpaper().compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (Throwable ignore) {
                Log.e(TAG, "save failed");
            }
            return info;
        }
    };

    Action1<LocalWallpaperInfo> setBgAction = new Action1<LocalWallpaperInfo>() {
        @Override
        public void call(LocalWallpaperInfo info) {
            layout.setBackground(new BitmapDrawable(info.getWallpaper()));
            detail.setText(info.getDate() + "\n" + info.getDescribption());
        }
    };

    private void setSystemWallpaper(Bitmap bitmap) {
        if (bitmap == null) {
            makeToast("bitmap == null");
            return;
        }
        WallpaperManager wallpaperManager = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
        try {
            wallpaperManager.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
