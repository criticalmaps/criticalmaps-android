package de.stephanlindauer.criticalmaps;

import android.app.Application;

import org.jetbrains.annotations.NotNull;
import org.maplibre.android.MapLibre;

import timber.log.Timber;

public class App extends Application {

    private static AppComponent appComponent;

    public static AppComponent components() {
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new NoOpTree());
        }

        appComponent = DaggerAppComponent.builder().app(this).build();
        MapLibre.getInstance(this);
    }

    private static class NoOpTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, @NotNull String message, Throwable t) {
        }
    }
}
