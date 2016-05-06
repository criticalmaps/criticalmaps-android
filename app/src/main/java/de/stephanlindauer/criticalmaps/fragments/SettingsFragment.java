package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.RoutesModel;
import de.stephanlindauer.criticalmaps.vo.RouteConfiguration;
import de.stephanlindauer.criticalmaps.vo.RoutesCategory;

public class SettingsFragment extends Fragment {

    @Bind(R.id.routes_settings_container)
    LinearLayout routesSettingsContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        for (RoutesCategory routesCategory : RoutesModel.getInstance().routesCategories) {

            final LinearLayout routesCategoryView = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.view_routes_category, null);
            routesSettingsContainer.addView(routesCategoryView);

            final Switch sternfahrtRoutesSwitch = (Switch) routesCategoryView.findViewById(R.id.sternfahrt_routes_button);
            sternfahrtRoutesSwitch.setText(routesCategory.getName());

            for (RouteConfiguration routesConfiguration : routesCategory.getRouteConfigurations()) {
                final LinearLayout sternfahrtRoutesCheckboxes = (LinearLayout) routesCategoryView.findViewById(R.id.sternfahrt_routes_checkboxes);

                final CheckBox routesCheckboxView = (CheckBox) LayoutInflater.from(getActivity()).inflate(R.layout.view_routes_route, null);
                routesCheckboxView.setText(routesConfiguration.getName());
                sternfahrtRoutesCheckboxes.addView(routesCheckboxView);

                sternfahrtRoutesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        sternfahrtRoutesCheckboxes.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    }
                });
            }
        }


//        sternfahrtRoutesButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                sternfahrtRoutesCheckboxes.setVisibility(isChecked ? View.VISIBLE : View.GONE);
//            }
//        });

        if (savedInstanceState != null) {
            //TODO
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}

