package de.stephanlindauer.criticalmass_berlin.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import de.stephanlindauer.criticalmass_berlin.R;

public class TwitterFragment extends SuperFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.twitter, container, false);

        return rootView;
    }
}