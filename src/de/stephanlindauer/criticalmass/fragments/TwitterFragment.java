package de.stephanlindauer.criticalmass.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.stephanlindauer.criticalmass.R;

public class TwitterFragment extends SuperFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.twitter, container, false);

        return rootView;
    }
}