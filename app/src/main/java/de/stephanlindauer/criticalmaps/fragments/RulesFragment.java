package de.stephanlindauer.criticalmaps.fragments;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.stephanlindauer.criticalmaps.R;

public class RulesFragment extends Fragment {

    private static final String KEY_ACTIVE_PANEL_ID = "active_panel_id";

    private View currentlyShownPanel;

    @BindView(R.id.rules_subcontainer)
    LinearLayout rulesSubContainer;

    private Unbinder unbinder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_rules, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
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

        if (savedInstanceState != null) {
            Integer activePanelId = (Integer) savedInstanceState.get(KEY_ACTIVE_PANEL_ID);
            if (activePanelId != null) {
                currentlyShownPanel = findViewById(activePanelId);
                currentlyShownPanel.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        // enable layout transition animations here to prevent animation on recreation
        LayoutTransition layoutTransition = new LayoutTransition();
        rulesSubContainer.setLayoutTransition(layoutTransition);
    }

    private void prepareButtonToPanel(@IdRes int button, @IdRes int panel) {
        final View panelView = findViewById(panel);
        panelView.setVisibility(View.GONE);
        final PanelShowingOnClickListener onClickListener = new PanelShowingOnClickListener(panelView);
        findViewById(button).setOnClickListener(onClickListener);
    }

    private View findViewById(@IdRes final int resId) {
        //noinspection ConstantConditions
        return getView().findViewById(resId);
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
            if (currentlyShownPanel == panel) {
                currentlyShownPanel = null;
            } else {
                currentlyShownPanel = panel;
                panel.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (currentlyShownPanel != null) {
            outState.putInt(KEY_ACTIVE_PANEL_ID, currentlyShownPanel.getId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
