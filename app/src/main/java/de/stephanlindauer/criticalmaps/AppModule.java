package de.stephanlindauer.criticalmaps;

import android.content.Context;
import android.content.SharedPreferences;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import okhttp3.OkHttpClient;

@Module
abstract class AppModule {
    @Provides
    @Singleton
    static OkHttpClient provideOKHttpClient() {
        return new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).build();
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
