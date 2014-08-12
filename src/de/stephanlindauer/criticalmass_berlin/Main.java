package de.stephanlindauer.criticalmass_berlin;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import de.stephanlindauer.criticalmass_berlin.adapter.TabsPagerAdapter;

public class Main extends FragmentActivity implements ActionBar.TabListener {

    ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        viewPager = (ViewPager) findViewById(R.id.pager);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled( true );

        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter( tabsPagerAdapter );

        actionBar.addTab( actionBar.newTab().setText(R.string.section_map ).setTabListener(this) );
        actionBar.addTab( actionBar.newTab().setText(R.string.section_rules).setTabListener(this) );
        actionBar.addTab( actionBar.newTab().setText(R.string.section_twitter).setTabListener(this) );
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem( tab.getPosition() );
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}
