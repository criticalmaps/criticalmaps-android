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

    private View.OnClickListener clickListerner = new View.OnClickListener() {
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
        View rootView = inflater.inflate(R.layout.about, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button facebookButton = (Button) getActivity().findViewById(R.id.about_facebook);
        Button twitterButton = (Button) getActivity().findViewById(R.id.about_twitter);

        facebookButton.setOnClickListener(clickListerner);
        twitterButton.setOnClickListener(clickListerner);
    }

    private String getUrlForId(int id) {
        String url;

        switch (id) {
            case R.id.about_facebook:
                url = "https://www.facebook.com/criticalmaps";
                break;
            case R.id.about_twitter:
                url = "https://twitter.com/CriticalMaps";
                break;
            default:
                url = "google.de";
                break;
        }

        return url;
    }

}