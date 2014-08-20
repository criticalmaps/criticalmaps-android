package de.stephanlindauer.criticalmass_berlin.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import de.stephanlindauer.criticalmass_berlin.R;

public class RulesFragment extends SuperFragment {

    private FragmentActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.rules, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();

        Button btnCorken = (Button) activity.findViewById(R.id.btnCorken);
        Button btnOncomingTraffic = (Button) activity.findViewById(R.id.btnSettings);
        Button btnPrivacy = (Button) activity.findViewById(R.id.btnPrivacy);

        View panelProfile = activity.findViewById(R.id.panelCorken);
        panelProfile.setVisibility(View.GONE);

        View panelSettings = activity.findViewById(R.id.panelOncomingTraffic);
        panelSettings.setVisibility(View.GONE);

        View panelPrivacy = activity.findViewById(R.id.panelPrivacy);
        panelPrivacy.setVisibility(View.GONE);

        btnCorken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View panelProfile = activity.findViewById(R.id.panelCorken);
                panelProfile.setVisibility(View.VISIBLE);

                View panelSettings = activity.findViewById(R.id.panelOncomingTraffic);
                panelSettings.setVisibility(View.GONE);

                View panelPrivacy = activity.findViewById(R.id.panelPrivacy);
                panelPrivacy.setVisibility(View.GONE);

            }
        });

        btnOncomingTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View panelProfile = activity.findViewById(R.id.panelCorken);
                panelProfile.setVisibility(View.GONE);

                View panelSettings = activity.findViewById(R.id.panelOncomingTraffic);
                panelSettings.setVisibility(View.VISIBLE);

                View panelPrivacy = activity.findViewById(R.id.panelPrivacy);
                panelPrivacy.setVisibility(View.GONE);

            }
        });

        btnPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View panelProfile = activity.findViewById(R.id.panelCorken);
                panelProfile.setVisibility(View.GONE);

                View panelSettings = activity.findViewById(R.id.panelOncomingTraffic);
                panelSettings.setVisibility(View.GONE);

                View panelPrivacy = activity.findViewById(R.id.panelPrivacy);
                panelPrivacy.setVisibility(View.VISIBLE);

            }
        });

    }
}