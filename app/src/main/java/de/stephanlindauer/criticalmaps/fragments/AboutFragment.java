package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.IntentUtil.URLOpenOnActivityOnClickListener;

public class AboutFragment extends Fragment {

    @Bind(R.id.about_facebook)
    ImageButton facebookButton;

    @Bind(R.id.about_twitter)
    ImageButton twitterButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        facebookButton.setOnClickListener(new URLOpenOnActivityOnClickListener("https://www.facebook.com/criticalmaps"));
        twitterButton.setOnClickListener(new URLOpenOnActivityOnClickListener("https://twitter.com/CriticalMaps"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
