package de.stephanlindauer.criticalmaps;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Module
abstract class AppModule {
    @Provides
    @Singleton
    static OkHttpClient provideOKHttpClient() {

        class UserAgentInterceptor implements Interceptor {
            private final String userAgent;

            public UserAgentInterceptor() {
                userAgent = BuildConfig.APPLICATION_ID
                        + "/" + BuildConfig.VERSION_NAME
                        + " (stephanlindauer@posteo.de, criticalmaps.net)";
            }

            @NonNull
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request requestWithUserAgent = originalRequest.newBuilder()
                        .removeHeader("User-Agent")
                        .header("User-Agent", userAgent)
                        .build();
                return chain.proceed(requestWithUserAgent);
            }
        }

        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new UserAgentInterceptor())
                .build();
    }

    @Provides
    @Singleton
    static Picasso providePicasso(App app, OkHttpClient client) {
        return new Picasso.Builder(app).downloader(new OkHttp3Downloader(client)).build();
    }

    @Provides
    @Reusable
    static SharedPreferences provideSharedPreferences(App app) {
        return app.getSharedPreferences("Main", Context.MODE_PRIVATE);
    }
}
