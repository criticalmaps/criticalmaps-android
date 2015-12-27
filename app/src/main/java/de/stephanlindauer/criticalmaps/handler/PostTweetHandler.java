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
        Intent twitterAppIntent = getTwitterAppIntent();
        Intent intentToStart = twitterAppIntent != null ? twitterAppIntent : getFallbackWebIntent();

        activity.startActivity(intentToStart);
    }

    private Intent getTwitterAppIntent() {
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, "#" + HASHTAG);
        tweetIntent.setType("text/plain");

        PackageManager packManager = activity.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                return tweetIntent;
            }
        }
        return null;
    }

    private Intent getFallbackWebIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(FALLBACK_URL));
        return intent;
    }
}
