package de.stephanlindauer.criticalmass;

import android.app.*;

import android.app.ActionBar;
import android.app.FragmentTransaction;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.stephanlindauer.criticalmass.adapter.TabsPagerAdapter;
import de.stephanlindauer.criticalmass.helper.CustomViewPager;
import de.stephanlindauer.criticalmass.helper.SelfDestructor;
import de.stephanlindauer.criticalmass.notifications.reminder.ReminderNotificationSetter;
import de.stephanlindauer.criticalmass.notifications.trackinginfo.TrackingInfoNotificationSetter;
import de.stephanlindauer.criticalmass.twitter.ITweetListener;
import de.stephanlindauer.criticalmass.twitter.Tweet;
import de.stephanlindauer.criticalmass.twitter.TwitterApi;
import de.stephanlindauer.criticalmass.utils.AsyncCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twitter4j.Status;

import java.util.Date;
import java.util.List;

public class Main extends FragmentActivity implements ActionBar.TabListener {

    CustomViewPager viewPager;
    public static final String TAG = "CriticalMass";

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

        SelfDestructor.getInstance().keepAlive();
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        SelfDestructor.getInstance().keepAlive();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SelfDestructor.getInstance().keepAlive();
    }
}
