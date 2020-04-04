package de.stephanlindauer.criticalmaps;

import android.content.SharedPreferences;

import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import de.stephanlindauer.criticalmaps.fragments.ChatFragment;
import de.stephanlindauer.criticalmaps.fragments.MapFragment;
import de.stephanlindauer.criticalmaps.fragments.SettingsFragment;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.TwitterModel;
import de.stephanlindauer.criticalmaps.provider.StorageLocationProvider;
import de.stephanlindauer.criticalmaps.service.ServerSyncService;
import okhttp3.OkHttpClient;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(Main main);
    void inject(ServerSyncService serverSyncService);
    void inject(MapFragment serverSyncService);
    void inject(ChatFragment chatFragment);
	void inject(SettingsFragment settingsFragment);

    App app();
    Picasso picasso();
    OwnLocationModel ownLocationmodel();
    TwitterModel twitterModel();
    OkHttpClient okHttpClient();
    SharedPreferences sharedPreferences();
    StorageLocationProvider storageProvider();

    @Component.Builder
    interface Builder {
        AppComponent build();

        @BindsInstance
        Builder app(App app);
    }
}
