package de.stephanlindauer.criticalmaps.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.stephanlindauer.criticalmaps.R;

public class AboutFragment extends SuperFragment {

    private View.OnClickListener clickListener = new View.OnClickListener() {
        public void onClick(View view) {
            String url = getUrlForId(view.getId());
            Intent websiteIntent = new Intent(android.content.Intent.ACTION_VIEW);
            websiteIntent.setData(Uri.parse(url));
            startActivity(websiteIntent);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button facebookButton = (Button) getActivity().findViewById(R.id.about_facebook);
        Button twitterButton = (Button) getActivity().findViewById(R.id.about_twitter);

        facebookButton.setOnClickListener(clickListener);
        twitterButton.setOnClickListener(clickListener);
    }

    private static String getUrlForId(int id) {
        switch (id) {
            case R.id.about_facebook:
                return "https://www.facebook.com/criticalmaps";
            case R.id.about_twitter:
                return "https://twitter.com/CriticalMaps";
            default:
                return "";
        }
    }

}