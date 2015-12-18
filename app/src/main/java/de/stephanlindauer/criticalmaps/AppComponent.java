package de.stephanlindauer.criticalmaps;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Component;
import de.stephanlindauer.criticalmaps.fragments.ChatFragment;
import de.stephanlindauer.criticalmaps.fragments.MapFragment;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.TwitterModel;
import de.stephanlindauer.criticalmaps.service.LocationUpdatesService;
import de.stephanlindauer.criticalmaps.service.ServerSyncService;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(Main main);

    void inject(ServerSyncService serverSyncService);

    void inject(MapFragment serverSyncService);

    void inject(ChatFragment chatFragment);

    Picasso picasso();

    OwnLocationModel ownLocationmodel();

    LocationUpdatesService locationUpdatesService();

    TwitterModel twitterModel();

    OkHttpClient okHttpClient();
}
