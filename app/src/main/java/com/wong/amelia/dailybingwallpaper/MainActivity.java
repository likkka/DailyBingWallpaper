package com.wong.amelia.dailybingwallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.wong.amelia.dailybingwallpaper.controller.RxController;
import com.wong.amelia.dailybingwallpaper.controller.UiController;
import com.wong.amelia.dailybingwallpaper.model.LocalWallpaperInfo;
import com.wong.amelia.dailybingwallpaper.utils.WallpaperHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, UiController {
    public static final String TAG = "RIO";
    public static boolean DBG = true;
    FrameLayout layout;
    Button button;
    TextView detail;
    ProgressBar progressBar;
    RxController rxController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViews();
        rxController = new RxController(this, this);

    }

    @Override
    public void showOrHideProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show?  View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    @Override
    public void refreshWidgetUi(final LocalWallpaperInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WallpaperHelper.updateWidget(MainActivity.this, info);
            }
        });
    }

    @Override
    public void refreshMainUi(final LocalWallpaperInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layout.setBackground(new BitmapDrawable(info.getWallpaper()));
                detail.setText(info.getDate() + "\n" + info.getDescribption());
            }
        });
    }

    private void setUpViews() {
        layout = (FrameLayout) findViewById(R.id.bg);
        detail = (TextView) findViewById(R.id.describe);
        button = (Button) findViewById(R.id.button);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        rxController.loadNewWallpaper();
    }

    public static void makeToast(Context context, String string) {
        //只能在main线程中
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setSystemWallpaper(Bitmap bitmap) {
        WallpaperHelper.setWallpaper(bitmap, this);
    }
}
