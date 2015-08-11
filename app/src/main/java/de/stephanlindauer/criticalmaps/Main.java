package de.stephanlindauer.criticalmaps;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import de.stephanlindauer.criticalmaps.adapter.TabsPagerAdapter;
import de.stephanlindauer.criticalmaps.helper.CustomViewPager;
import de.stephanlindauer.criticalmaps.model.UserModel;
import de.stephanlindauer.criticalmaps.notifications.trackinginfo.TrackingInfoNotificationSetter;
import de.stephanlindauer.criticalmaps.service.GPSMananger;
import de.stephanlindauer.criticalmaps.service.ServerPuller;

public class Main extends FragmentActivity implements ActionBar.TabListener {

    //dependencies
    private final TrackingInfoNotificationSetter trackingInfoNotificationSetter = TrackingInfoNotificationSetter.getInstance();
    private final ServerPuller serverPuller = ServerPuller.getInstance();
    private final GPSMananger gpsMananger = GPSMananger.getInstance();
    private final UserModel userModel = UserModel.getInstance();

    //misc
    private CustomViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        setupViewPager();

        initializeNotifications();

        serverPuller.initialize(this);
        gpsMananger.initialize(this);
    }

    private void initializeNotifications() {
        trackingInfoNotificationSetter.initialize(getBaseContext(), this);
        trackingInfoNotificationSetter.show();

        userModel.initialize(this);
    }

    private void setupViewPager() {
        viewPager = (CustomViewPager) findViewById(R.id.pager);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsPagerAdapter);

        actionBar.addTab(actionBar.newTab().setText(R.string.section_map).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_chat).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_rules).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_about).setTabListener(this));

        registerListenersForSwipedChanges(actionBar);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
