package de.stephanlindauer.criticalmass;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import de.stephanlindauer.criticalmass.adapter.TabsPagerAdapter;
import de.stephanlindauer.criticalmass.helper.CustomViewPager;
import de.stephanlindauer.criticalmass.notifications.reminder.ReminderNotificationSetter;
import de.stephanlindauer.criticalmass.notifications.trackinginfo.TrackingInfoNotificationSetter;
import de.stephanlindauer.criticalmass.twitter.ITweetListener;
import de.stephanlindauer.criticalmass.twitter.Tweet;
import de.stephanlindauer.criticalmass.twitter.TwitterApi;
import de.stephanlindauer.criticalmass.utils.AsyncCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twitter4j.HashtagEntity;
import twitter4j.Status;

import java.util.List;

public class Main extends FragmentActivity implements ActionBar.TabListener {

    CustomViewPager viewPager;
    private static final String TAG = "CriticalMass";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        viewPager = (CustomViewPager) findViewById(R.id.pager);

        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsPagerAdapter);

        actionBar.addTab(actionBar.newTab().setText(R.string.section_map).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_rules).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_twitter).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_about).setTabListener(this));

        registerListenersForSwipedChanges(actionBar);

        ReminderNotificationSetter reminderNotificationSetter = new ReminderNotificationSetter(getBaseContext(), this);
        reminderNotificationSetter.execute();

        TrackingInfoNotificationSetter.getInstance().initialize(getBaseContext(), this);
        TrackingInfoNotificationSetter.getInstance().show();
    }

    private void registerListenersForSwipedChanges(final ActionBar actionBar) {
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private TwitterApi twitter;
    public static final String TWITTER_CRITICAL_MASS_HASHTAG = "#CMBerlin";
    public static final int TWITTER_MAX_FEED = 10;
    public static final String TWITTER_SINCE = "2014-01-01";

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());

        if (tab.getText().equals(getResources().getString(R.string.section_twitter))) {

            final TextView view = (TextView) viewPager.findViewById(R.id.twittertextview);

            if (twitter == null) {
                twitter = new TwitterApi(this);

                // new feeds
                twitter.searchTweets(new String[]{TWITTER_CRITICAL_MASS_HASHTAG}, new ITweetListener() {
                    @Override
                    public void onNewTweet(@NotNull final Tweet tweet) {
                        Log.v(TAG, "ITweetListener onNewTweet: " + tweet);
                        view.append(tweet.toString());
                    }

                    @Override
                    public void onException(@NotNull final Exception e) {
                        Log.e(TAG, "ITweetListener onException " + e.getMessage());
                    }
                });

                // get past feeds
                twitter.searchTweetsAsync(TWITTER_CRITICAL_MASS_HASHTAG, new AsyncCallback() {

                    @Override
                    public void onComplete(@Nullable final Object result) {
                        if (!(result instanceof List<?>))
                            return;

                        final List<Status> stati = (List<Status>) result;

                        for (final Status status : stati) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    view.append(Html.fromHtml("<p>" + status.getUser().getScreenName() + ":" + status.getText() + "</p>"));
                                    Log.v(TAG, "Tweet: " + status.getUser().getName() + " : " + status.getText() + "");
                                }
                            });

                        }
                    }

                    @Override
                    public void onError(final Exception e) {
                        Log.e(TAG, "" + e.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
}
