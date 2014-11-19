package de.stephanlindauer.criticalmaps.helper;


import android.os.Handler;
import de.stephanlindauer.criticalmaps.notifications.trackinginfo.TrackingInfoNotificationSetter;

public class SelfDestructor {

    private static SelfDestructor instance;

    private final long IDLE_TIME_UNTIL_AUTO_DESTRUCT = 4 * 60 * 60 * 1000; // 4 hours

    private Handler handler;

    private Runnable closeApp = new Runnable() {
        @Override
        public void run() {
            TrackingInfoNotificationSetter.getInstance().cancel();
            System.exit(0);
        }
    };

    public static SelfDestructor getInstance() {
        if (SelfDestructor.instance == null) {
            SelfDestructor.instance = new SelfDestructor();
        }
        return SelfDestructor.instance;
    }

    public void keepAlive() {
        if (handler == null) {
            handler = new Handler();
        }
        handler.removeCallbacks(closeApp);
        handler.postDelayed(closeApp, IDLE_TIME_UNTIL_AUTO_DESTRUCT);
    }
}
