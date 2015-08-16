package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.adapter.ChatMessageAdapter;
import de.stephanlindauer.criticalmaps.handler.TwitterGetHandler;

public class TwitterFragment extends SuperFragment {

    //view
    private View twitterView;

    //adapter
    private ChatMessageAdapter chatMessageAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        twitterView = inflater.inflate(R.layout.fragment_twitter, container, false);
        return twitterView;
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);
        new TwitterGetHandler().execute();
    }
}