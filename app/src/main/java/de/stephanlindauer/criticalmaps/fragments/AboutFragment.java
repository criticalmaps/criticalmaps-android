package de.stephanlindauer.criticalmaps.fragments;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import de.stephanlindauer.criticalmaps.databinding.FragmentAboutBinding;
import de.stephanlindauer.criticalmaps.utils.IntentUtil.URLOpenOnActivityOnClickListener;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LayoutTransition layoutTransition = binding.aboutContentLayout.getLayoutTransition();
        // make LicensePanelView animations look nice
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        // stop settings from bubbling up to ScrollView to prevent scroll animation
        // on state restore and panel close
        layoutTransition.setAnimateParentHierarchy(false);
        binding.aboutContentLayout.setLayoutTransition(layoutTransition);

        binding.aboutFacebookButton.setOnClickListener(
                new URLOpenOnActivityOnClickListener("https://www.facebook.com/criticalmaps"));
        binding.aboutInstagramButton.setOnClickListener(
                new URLOpenOnActivityOnClickListener("https://instagram.com/CriticalMaps"));
        binding.aboutGithubButton.setOnClickListener(
                new URLOpenOnActivityOnClickListener("https://github.com/criticalmaps/criticalmaps-android"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
