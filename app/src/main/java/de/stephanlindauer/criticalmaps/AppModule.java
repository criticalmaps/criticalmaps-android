package de.stephanlindauer.criticalmaps;

import android.content.Context;
import android.content.SharedPreferences;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;
import de.stephanlindauer.criticalmaps.handler.NetworkConnectivityChangeHandler;
import de.stephanlindauer.criticalmaps.handler.ServerResponseProcessor;
import de.stephanlindauer.criticalmaps.managers.LocationUpdateManager;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.TwitterModel;
import de.stephanlindauer.criticalmaps.model.UserModel;
import de.stephanlindauer.criticalmaps.provider.EventBus;

import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

@Module
public class AppModule {

    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public App provideApp() {
        return app;
    }

    @Provides
    @Singleton
    public OkHttpClient provideOKHttpClient() {
        return new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).build();
    }

    @Provides
    @Singleton
    public Picasso providePicasso(App app, OkHttpClient client) {
        return new Picasso.Builder(app).downloader(new OkHttp3Downloader(client)).build();
    }

    @Provides
    @Singleton
    public ChatModel provideChatModel() {
        return new ChatModel();
    }

    @Provides
    @Singleton
    public UserModel provideUserModel() {
        return new UserModel(app);
    }


    @Provides
    @Singleton
    public TwitterModel provideTwitterModel() {
        return new TwitterModel();
    }

    @Provides
    @Singleton
    public OwnLocationModel provideOwnLocationModel() {
        return new OwnLocationModel();
    }

    @Provides
    @Singleton
    public ServerResponseProcessor serverResponseProcessor(
            OtherUsersLocationModel otherUsersLocationModel,
            EventBus eventBus,
            ChatModel chatModel) {
        return new ServerResponseProcessor(otherUsersLocationModel, eventBus, chatModel);
    }

    @Provides
    @Singleton
    public OtherUsersLocationModel provideOtherUsersLocationModel() {
        return new OtherUsersLocationModel();
    }

    @Provides
    @Singleton
    public EventBus provideEventBus() {
        return new EventBus();
    }

    @Provides
    @Singleton
    public LocationUpdateManager provideLocationUpdateManager(
            OwnLocationModel ownLocationModel,
            EventBus eventBus) {
        return new LocationUpdateManager(app, ownLocationModel, eventBus);
    }

    @Provides
    @Singleton
    public NetworkConnectivityChangeHandler provideNetworkConnectivityChangeHandler(
            EventBus eventBus) {
        return new NetworkConnectivityChangeHandler(app, eventBus);
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return app.getSharedPreferences("Main", Context.MODE_PRIVATE);
    }
}
