package de.stephanlindauer.criticalmass.service;

import android.app.Activity;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import de.stephanlindauer.criticalmass.helper.AeSimpleSHA1;
import de.stephanlindauer.criticalmass.helper.ICommand;
import de.stephanlindauer.criticalmass.helper.RequestTask;
import de.stephanlindauer.criticalmass.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmass.model.OwnLocationModel;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.*;

public class ServerPuller {

    public static final int PULL_OTHER_LOCATIONS_TIME = 30000; //milliseconds

    private Activity activity;

    private Timer timerGettingOtherBikers;
    private TimerTask timerTaskGettingsOtherBikers;

    private String uniqueDeviceIdHashed;

    private static ServerPuller instance;

    public static ServerPuller getInstance() {
        if (ServerPuller.instance == null) {
            ServerPuller.instance = new ServerPuller();
        }
        return ServerPuller.instance;
    }

    public void initialize(final Activity activity) {
        this.activity = activity;
        this.uniqueDeviceIdHashed = AeSimpleSHA1.SHA1(Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID));

        timerGettingOtherBikers = new Timer();

        timerTaskGettingsOtherBikers = new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(
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
        RequestTask request = new RequestTask(uniqueDeviceIdHashed, OwnLocationModel.getInstance().ownLocation, new ICommand() {
            public void execute(String... payload) {
                try {
                    JSONObject jsonObject = new JSONObject(payload[0]);
                    Iterator<String> keys = jsonObject.keys();

                    OtherUsersLocationModel.getInstance().otherUsersLocations = new ArrayList<GeoPoint>();

                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject value = jsonObject.getJSONObject(key);
                        Integer latitude = Integer.parseInt(value.getString("latitude"));
                        Integer longitude = Integer.parseInt(value.getString("longitude"));

                        OtherUsersLocationModel.getInstance().otherUsersLocations.add(new GeoPoint(latitude, longitude));
                    }
                } catch (Exception e) {
                    return;
                }
            }
        });
        request.execute();
    }
}