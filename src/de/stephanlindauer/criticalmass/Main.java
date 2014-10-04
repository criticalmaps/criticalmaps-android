package de.stephanlindauer.criticalmass;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import de.stephanlindauer.criticalmass.adapter.TabsPagerAdapter;
import de.stephanlindauer.criticalmass.helper.CustomViewPager;
import de.stephanlindauer.criticalmass.service.ServerPuller;
import de.stephanlindauer.criticalmass.helper.SelfDestructor;
import de.stephanlindauer.criticalmass.notifications.reminder.ReminderNotificationSetter;
import de.stephanlindauer.criticalmass.notifications.trackinginfo.TrackingInfoNotificationSetter;
import de.stephanlindauer.criticalmass.vo.City;

public class Main extends FragmentActivity implements ActionBar.TabListener {

    public static final String TAG = "CriticalMass";
    CustomViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setupViewPager();


        ReminderNotificationSetter reminderNotificationSetter = new ReminderNotificationSetter(getBaseContext(), this);
        reminderNotificationSetter.execute();

        TrackingInfoNotificationSetter.getInstance().initialize(getBaseContext(), this);
        TrackingInfoNotificationSetter.getInstance().show();

        SelfDestructor.getInstance().keepAlive();

        ServerPuller.getInstance().initialize(this);
//        showCityChooserDialog();
    }

    private void setupViewPager() {
        viewPager = (CustomViewPager) findViewById(R.id.pager);

        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsPagerAdapter);

        actionBar.addTab(actionBar.newTab().setText(R.string.section_map).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_rules).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_chat).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_about).setTabListener(this));

        registerListenersForSwipedChanges(actionBar);
    }

    private void showCityChooserDialog() {

        final AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(getBaseContext().getString(R.string.city_chooser_title));
        ad.setItems(City.getAvailableCities(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println();
            }
        });

        ad.show();
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
