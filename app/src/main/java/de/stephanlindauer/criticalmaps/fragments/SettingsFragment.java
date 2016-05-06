package de.stephanlindauer.criticalmaps.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import org.apache.commons.lang3.Range;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.RoutesModel;
import de.stephanlindauer.criticalmaps.vo.RouteConfiguration;
import de.stephanlindauer.criticalmaps.vo.RoutesCategory;

public class SettingsFragment extends Fragment {

    @Inject
    SharedPreferences sharedPreferences;

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
        App.components().inject(this);

        for (final RoutesCategory routesCategory : RoutesModel.getInstance().routesCategories) {
            boolean isActive = routesCategory.isActive(sharedPreferences);

            final LinearLayout routesCategoryView = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.view_routes_category, null);
            routesSettingsContainer.addView(routesCategoryView);

            final Switch routesSwitch = (Switch) routesCategoryView.findViewById(R.id.sternfahrt_routes_button);
            routesSwitch.setText(routesCategory.getName());
            routesSwitch.setChecked(isActive);

            final LinearLayout routesCheckboxes = (LinearLayout) routesCategoryView.findViewById(R.id.sternfahrt_routes_checkboxes);
            routesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    routesCategory.setActive(isChecked, sharedPreferences);
                    routesCheckboxes.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            });

            routesCheckboxes.setVisibility(isActive ? View.VISIBLE : View.GONE);

            for (final RouteConfiguration routeConfiguration : routesCategory.getRouteConfigurations()) {
                final CheckBox routesCheckboxView = (CheckBox) LayoutInflater.from(getActivity()).inflate(R.layout.view_routes_route, null);
                routesCheckboxView.setText(routeConfiguration.getName());
              routesCheckboxView.setChecked(routeConfiguration.isActive(sharedPreferences));
                routesCheckboxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        routeConfiguration.setActive(isChecked, sharedPreferences);
                    }
                });
                routesCheckboxes.addView(routesCheckboxView);
            }
        }

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

