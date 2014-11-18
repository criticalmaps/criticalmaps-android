package de.stephanlindauer.criticalmass.service;

import android.app.Activity;
import android.provider.Settings;
import de.greenrobot.event.EventBus;
import de.stephanlindauer.criticalmass.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmass.helper.AeSimpleSHA1;
import de.stephanlindauer.criticalmass.helper.ICommand;
import de.stephanlindauer.criticalmass.helper.RequestTask;
import de.stephanlindauer.criticalmass.model.ChatModel;
import de.stephanlindauer.criticalmass.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmass.model.OwnLocationModel;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class ServerPuller {

    //model
    private OtherUsersLocationModel otherUsersLocationModel = OtherUsersLocationModel.getInstance();
    private ChatModel chatModel = ChatModel.getInstance();

    //const
    public static final int PULL_OTHER_LOCATIONS_TIME = 20 * 1000; //20 sec

    private Activity activity;

    private Timer timerPullServer;
    private TimerTask timerTaskPullServer;

    private String uniqueDeviceIdHashed;
    private String message;


    boolean currentlyRunningARequest = false;

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

        timerPullServer = new Timer();

        timerTaskPullServer = new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                pullServer();
                            }
                        }
                );
            }
        };
        timerPullServer.scheduleAtFixedRate(timerTaskPullServer, 0, PULL_OTHER_LOCATIONS_TIME);
    }

    public void addOutGoingMessageAndTriggerRequest(String message) {
        this.message = message;
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        pullServer();
                    }
                }
        );
    }

    private void pullServer() {

        if (currentlyRunningARequest == true)
            return;

        currentlyRunningARequest = true;

        RequestTask request = new RequestTask(uniqueDeviceIdHashed, OwnLocationModel.getInstance().ownLocation, message, new ICommand() {
            public void execute(String... payload) {
                try {
                    if (payload[0] == RequestTask.ERROR_STRING)
                        throw new Exception();

                    JSONObject jsonObject = new JSONObject(payload[0]);
                    otherUsersLocationModel.setNewJSON(jsonObject.getJSONObject("locations"));
                    chatModel.setNewJson(jsonObject.getJSONObject("chatMessages"));

                    EventBus.getDefault().post( new NewServerResponseEvent());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    currentlyRunningARequest = false;
                }
            }
        });

        request.execute();
    }
}