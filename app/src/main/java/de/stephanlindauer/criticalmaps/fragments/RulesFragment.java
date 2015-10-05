package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.stephanlindauer.criticalmaps.R;

public class RulesFragment extends Fragment {

    private View currentlyShownPanel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_rules, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prepareButtonToPanel(R.id.btnCorken, R.id.panelCorken);
        prepareButtonToPanel(R.id.btnOncomingTraffic, R.id.panelOncomingTraffic);
        prepareButtonToPanel(R.id.btnSlow, R.id.panelSlow);
        prepareButtonToPanel(R.id.btnBrake, R.id.panelBrake);
        prepareButtonToPanel(R.id.btnFriendly, R.id.panelFriendly);
        prepareButtonToPanel(R.id.btnFun, R.id.panelFun);
        prepareButtonToPanel(R.id.btnGreen, R.id.panelGreen);
    }

    private void prepareButtonToPanel(@IdRes int button, @IdRes int panel) {
        final View panelView = findViewById(panel);
        panelView.setVisibility(View.GONE);
        final PanelShowingOnClickListener onClickListener = new PanelShowingOnClickListener(panelView);
        findViewById(button).setOnClickListener(onClickListener);
    }

    private View findViewById(@IdRes final int resId) {
        return getActivity().findViewById(resId);
    }

    private class PanelShowingOnClickListener implements View.OnClickListener {

        private final View panel;

        private PanelShowingOnClickListener(final View panel) {
            this.panel = panel;
        }

        @Override
        public void onClick(final View v) {
            if (currentlyShownPanel != null) {
                currentlyShownPanel.setVisibility(View.GONE);
            }
            currentlyShownPanel = panel;
            panel.setVisibility(View.VISIBLE);
        }
    }


}