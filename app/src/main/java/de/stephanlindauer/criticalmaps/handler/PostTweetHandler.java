package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.List;

public class PostTweetHandler {
    private final static String HASHTAG = "CriticalMaps";
    private final static String FALLBACK_URL = "https://twitter.com/intent/tweet?button_hashtag=" + HASHTAG;

    private final Activity activity;

    public PostTweetHandler(Activity activity) {
        this.activity = activity;
    }

    public void execute() {
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, "#" + HASHTAG);
        tweetIntent.setType("text/plain");

        PackageManager packManager = activity.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            activity.startActivity(tweetIntent);
        } else {
            launchFallbackIntent();
        }
    }

    private void launchFallbackIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(FALLBACK_URL));
        activity.startActivity(intent);
    }
}
