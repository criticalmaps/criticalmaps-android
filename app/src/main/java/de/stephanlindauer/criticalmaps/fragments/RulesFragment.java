package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.R;

public class RulesFragment extends SuperFragment {

    private FragmentActivity activity;
    private ArrayList<View> panels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.rules, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();

        panels = new ArrayList<View>();
        panels.add(activity.findViewById(R.id.panelCorken));
        panels.add(activity.findViewById(R.id.panelOncomingTraffic));
        panels.add(activity.findViewById(R.id.panelSlow));
        panels.add(activity.findViewById(R.id.panelBrake));
        panels.add(activity.findViewById(R.id.panelFriendly));
        panels.add(activity.findViewById(R.id.panelFun));
        panels.add(activity.findViewById(R.id.panelGreen));

        hideAllPanels();


        Button btnCorken = (Button) activity.findViewById(R.id.btnCorken);
        btnCorken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllPanels();
                activity.findViewById(R.id.panelCorken).setVisibility(View.VISIBLE);
            }
        });

        Button btnOncomingTraffic = (Button) activity.findViewById(R.id.btnOncomingTraffic);
        btnOncomingTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllPanels();
                activity.findViewById(R.id.panelOncomingTraffic).setVisibility(View.VISIBLE);
            }
        });

        Button btnSlow = (Button) activity.findViewById(R.id.btnSlow);
        btnSlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllPanels();
                activity.findViewById(R.id.panelSlow).setVisibility(View.VISIBLE);
            }
        });

        Button btnBrake = (Button) activity.findViewById(R.id.btnBrake);
        btnBrake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllPanels();
                activity.findViewById(R.id.panelBrake).setVisibility(View.VISIBLE);
            }
        });

        Button btnFriendly = (Button) activity.findViewById(R.id.btnFriendly);
        btnFriendly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllPanels();
                activity.findViewById(R.id.panelFriendly).setVisibility(View.VISIBLE);
            }
        });

        Button btnFun = (Button) activity.findViewById(R.id.btnFun);
        btnFun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllPanels();
                activity.findViewById(R.id.panelFun).setVisibility(View.VISIBLE);
            }
        });

        Button btnGreen = (Button) activity.findViewById(R.id.btnGreen);
        btnGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllPanels();
                activity.findViewById(R.id.panelGreen).setVisibility(View.VISIBLE);
            }
        });


    }

    private void hideAllPanels() {
        for (View view : panels) {
            view.setVisibility(View.GONE);
        }
    }

}