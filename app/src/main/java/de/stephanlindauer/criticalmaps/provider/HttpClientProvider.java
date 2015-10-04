package de.stephanlindauer.criticalmaps.provider;

import com.squareup.okhttp.OkHttpClient;
import java.util.concurrent.TimeUnit;

public class HttpClientProvider {

    public static final int TIME_OUT_IN_S = 15;

    private static OkHttpClient instance;

    public static OkHttpClient get() {
        if (instance == null) {
            instance = new OkHttpClient();
            instance.setConnectTimeout(TIME_OUT_IN_S, TimeUnit.SECONDS);
        }
        return instance;
    }
}
