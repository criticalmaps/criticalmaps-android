package de.stephanlindauer.criticalmass.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import de.stephanlindauer.criticalmass.R;

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

        Button paypalButton = (Button) getActivity().findViewById(R.id.about_paypal);
        Button facebookButton = (Button) getActivity().findViewById(R.id.about_facebook);
        Button twitterButton = (Button) getActivity().findViewById(R.id.about_twitter);

        paypalButton.setOnClickListener(clickListerner);
        facebookButton.setOnClickListener(clickListerner);
        twitterButton.setOnClickListener(clickListerner);
    }

    private String getUrlForId(int id) {
        String url = "";

        switch (id) {
            case R.id.about_paypal:
                url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=8DBW7Z768DQT6&lc=GB&no_note=0&cn=Mitteilung%20an%20den%20H%c3%a4ndler%3a&no_shipping=1&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted";
                break;
            case R.id.about_facebook:
                url = "https://www.facebook.com/pages/Critical-Mass-Berlin/74806304846";
                break;
            case R.id.about_twitter:
                url = "https://twitter.com/CMBerlin";
                break;
            default:
                url = "google.de";
                break;
        }

        return url;
    }

}