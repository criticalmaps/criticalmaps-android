package de.stephanlindauer.criticalmaps.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.stephanlindauer.criticalmaps.fragments.AboutFragment;
import de.stephanlindauer.criticalmaps.fragments.ChatFragment;
import de.stephanlindauer.criticalmaps.fragments.MapFragment;
import de.stephanlindauer.criticalmaps.fragments.RulesFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return new MapFragment();
            case 1:
                return new ChatFragment();
            case 2:
                return new RulesFragment();
            case 3:
                return new AboutFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
