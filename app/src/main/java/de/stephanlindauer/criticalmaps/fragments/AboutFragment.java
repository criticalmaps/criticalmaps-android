package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.IntentUtil.URLOpenOnActivityOnClickListener;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button facebookButton = (Button) getActivity().findViewById(R.id.about_facebook);
        Button twitterButton = (Button) getActivity().findViewById(R.id.about_twitter);

        facebookButton.setOnClickListener(new URLOpenOnActivityOnClickListener("https://www.facebook.com/criticalmaps"));
        twitterButton.setOnClickListener(new URLOpenOnActivityOnClickListener("https://twitter.com/CriticalMaps"));
    }

}