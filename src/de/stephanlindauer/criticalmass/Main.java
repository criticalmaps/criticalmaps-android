package de.stephanlindauer.criticalmass;

import android.app.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import de.stephanlindauer.criticalmass.adapter.TabsPagerAdapter;
import de.stephanlindauer.criticalmass.helper.CustomViewPager;
import de.stephanlindauer.criticalmass.notifications.reminder.ReminderNotificationSetter;
import de.stephanlindauer.criticalmass.notifications.trackinginfo.TrackingInfoNotificationSetter;

public class Main extends FragmentActivity implements ActionBar.TabListener {

    CustomViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        final Main thiss = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        viewPager = (CustomViewPager) findViewById(R.id.pager);

        final ActionBar actionBar = getActionBar();
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
}
