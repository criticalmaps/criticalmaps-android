package de.stephanlindauer.criticalmassberlin.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.stephanlindauer.criticalmassberlin.fragments.AboutFragment;
import de.stephanlindauer.criticalmassberlin.fragments.MapFragment;
import de.stephanlindauer.criticalmassberlin.fragments.RulesFragment;
import de.stephanlindauer.criticalmassberlin.fragments.TwitterFragment;

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
                return new RulesFragment();
            case 2:
                return new TwitterFragment();
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
