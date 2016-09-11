package de.stephanlindauer.criticalmaps.fragments;

import android.animation.LayoutTransition;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.stephanlindauer.criticalmaps.R;

public class RulesFragment extends Fragment {

    private static final String KEY_SCROLLVIEW_POSITION = "scrollview_position";
    private static final String KEY_ACTIVE_PANEL_ID = "active_panel_id";

    private View currentlyShownPanel;

    @BindView(R.id.rules_subcontainer)
    LinearLayout linearLayout;
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
                LayoutTransition layoutTransition = linearLayout.getLayoutTransition();
                long durationAppearing = layoutTransition.getDuration(LayoutTransition.APPEARING);
                layoutTransition.setDuration(LayoutTransition.APPEARING, 0);
                currentlyShownPanel.setVisibility(View.VISIBLE);
                layoutTransition.setDuration(LayoutTransition.APPEARING, durationAppearing);
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final int scrollviewPosition = savedInstanceState.getInt(KEY_SCROLLVIEW_POSITION, 0);

                if (scrollviewPosition != 0) {
                    final ScrollView scrollView = (ScrollView) findViewById(R.id.rules_scrollview);

                    scrollView.post(new Runnable() {
                        public void run() {
                            scrollView.scrollTo(0, scrollviewPosition);
                        }
                    });
                }
            }
        }
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
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            outState.putInt(KEY_SCROLLVIEW_POSITION, findViewById(R.id.rules_scrollview).getScrollY());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        currentlyShownPanel = null;
    }
}
