package de.stephanlindauer.criticalmaps.provider;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.fragments.AboutFragment;
import de.stephanlindauer.criticalmaps.fragments.ChatFragment;
import de.stephanlindauer.criticalmaps.fragments.MapFragment;
import de.stephanlindauer.criticalmaps.fragments.RulesFragment;
import de.stephanlindauer.criticalmaps.fragments.SettingsFragment;

public class FragmentProvider {

    public static Fragment getFragmentForNavId(@IdRes int navId) {
        switch (navId) {
            case R.id.navigation_map:
                return new MapFragment();

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
