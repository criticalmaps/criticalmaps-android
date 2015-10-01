package de.stephanlindauer.criticalmaps.adapter;

import android.app.Application;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.fragments.AboutFragment;
import de.stephanlindauer.criticalmaps.fragments.ChatFragment;
import de.stephanlindauer.criticalmaps.fragments.MapFragment;
import de.stephanlindauer.criticalmaps.fragments.RulesFragment;
import de.stephanlindauer.criticalmaps.fragments.TwitterFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    private final Application application;

    public TabsPagerAdapter(Application application, FragmentManager fm) {
        super(fm);
        this.application = application;
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return new MapFragment();
            case 1:
                return new ChatFragment();
            case 2:
                return new TwitterFragment();
            case 3:
                return new RulesFragment();
            case 4:
                return new AboutFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Resources res = application.getResources();
        switch (position) {
            case 0:
                return res.getString(R.string.section_map);
            case 1:
                return res.getString(R.string.section_chat);
            case 2:
                return res.getString(R.string.section_twitter);
            case 3:
                return res.getString(R.string.section_rules);
            case 4:
                return res.getString(R.string.section_about);
            default:
                return null;
        }
    }
}
