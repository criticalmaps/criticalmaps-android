package de.stephanlindauer.criticalmaps;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class App extends Application {

    private static AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new NoOpTree());
        }

        appComponent = DaggerAppComponent.builder().app(this).build();
    }

    public static AppComponent components() {
        return appComponent;
    }

    private static class NoOpTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, @NotNull String message, Throwable t) {
        }
    }
}
