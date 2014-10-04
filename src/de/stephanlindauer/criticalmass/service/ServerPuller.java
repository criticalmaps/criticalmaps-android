package de.stephanlindauer.criticalmass.service;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import de.stephanlindauer.criticalmass.helper.AeSimpleSHA1;
import de.stephanlindauer.criticalmass.helper.ICommand;
import de.stephanlindauer.criticalmass.helper.RequestTask;
import de.stephanlindauer.criticalmass.notifications.trackinginfo.TrackingInfoNotificationSetter;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.*;

public class ServerPuller {



    public static final int PULL_OTHER_LOCATIONS_TIME = 30000; //milliseconds

    private static ServerPuller instance;

    public GeoPoint userLocation = null;

    public List<GeoPoint> otherUsersLocations = new ArrayList<GeoPoint>();
    private FragmentActivity mContext;
    private Timer timerGettingOtherBikers;
    private TimerTask timerTaskGettingsOtherBikers;

    private boolean initialized = false;
    private String uniqueDeviceIdHashed;


    public static ServerPuller getInstance() {
        if (ServerPuller.instance == null) {
            ServerPuller.instance = new ServerPuller();
        }
        return ServerPuller.instance;
    }

    public void initialize(final FragmentActivity mContext) {
        if (initialized)
            return;

        this.mContext = mContext;
        this.initialized = true;

        try {
            this.uniqueDeviceIdHashed = AeSimpleSHA1.SHA1(Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID));
        } catch (Exception e) {
        }

        //start other bikes location retrieval
        timerGettingOtherBikers = new Timer();
        timerTaskGettingsOtherBikers = new TimerTask() {
            @Override
            public void run() {
                mContext.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                getOtherBikersInfoFromServer();
                            }
                        }
                );
            }
        };
        timerGettingOtherBikers.scheduleAtFixedRate(timerTaskGettingsOtherBikers, 0, PULL_OTHER_LOCATIONS_TIME);
    }



    private void getOtherBikersInfoFromServer() {
        RequestTask request = new RequestTask(uniqueDeviceIdHashed, userLocation, new ICommand() {
            public void execute(String... payload) {
                try {
                    JSONObject jsonObject = new JSONObject(payload[0]);
                    Iterator<String> keys = jsonObject.keys();

                    otherUsersLocations = new ArrayList<GeoPoint>();

                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject value = jsonObject.getJSONObject(key);
                        Integer latitude = Integer.parseInt(value.getString("latitude"));
                        Integer longitude = Integer.parseInt(value.getString("longitude"));

                        otherUsersLocations.add(new GeoPoint(latitude, longitude));
                    }
                } catch (Exception e) {
                    return;
                }
            }
        });
        request.execute();
    }

    public void shouldBeTrackingUsersLocation(boolean shouldBeTracking) {
        if (shouldBeTracking)
            startLocationListening();
        else
            stopLocationListening();
    }
}