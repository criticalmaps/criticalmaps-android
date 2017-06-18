package de.stephanlindauer.criticalmaps;

import android.app.Application;

import de.stephanlindauer.criticalmaps.service.ServerSyncService;

public class App extends Application {

    private static AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        ServerSyncService.startService();
    }

    public static AppComponent components() {
        return appComponent;
    }
}
