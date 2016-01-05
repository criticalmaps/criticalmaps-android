package de.stephanlindauer.criticalmaps.provider;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.fragments.*;

public class FragmentProvider {

    public static Fragment getFragmentForNavId(@IdRes int navId) {
        switch (navId) {
            case R.id.navigation_map:
                return new MapFragment();

            case R.id.navigation_twitter:
                return new TwitterFragment();

            case R.id.navigation_rules:
                return new RulesFragment();

            case R.id.navigation_chat:
                return new ChatFragment();

            case R.id.navigation_settings:
                return new SettingsFragment();

            default:
            case R.id.navigation_about:
                return new AboutFragment();

        }
    }
}
