package com.wong.amelia.dailybingwallpaper.model;

import com.squareup.okhttp.OkHttpClient;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by amelia on 3/10/16.
 */
public class ServiceGenerator {
    public static final String BING_WP_URL = "http://www.bing.com";
    private static OkHttpClient builder = new OkHttpClient();
    private static Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
            .client(builder)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BING_WP_URL);
//    public static <S> S createRxService(Class<S> service) {
//        return retrofitBuilder.addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build().create(service);
//    }
    public static <S> S createService(Class<S> service) {
        return retrofitBuilder.build().create(service);
    }
}
