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
        if (navId == R.id.navigation_map) {
            return new MapFragment();
        } else if (navId == R.id.navigation_rules) {
            return new RulesFragment();
        } else if (navId == R.id.navigation_chat) {
            return new ChatFragment();
        } else if (navId == R.id.navigation_settings) {
            return new SettingsFragment();
        } else { // R.id.navigation_about
            return new AboutFragment();
        }
    }
}
