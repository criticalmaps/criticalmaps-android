package de.stephanlindauer.criticalmaps.fragments;

import android.animation.LayoutTransition;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.IntentUtil.URLOpenOnActivityOnClickListener;

public class AboutFragment extends Fragment {

    private static final String KEY_SCROLLVIEW_POSITION = "scrollview_position";

    @BindView(R.id.about_facebook)
    ImageButton facebookButton;

    @BindView(R.id.about_twitter)
    ImageButton twitterButton;

    @BindView(R.id.about_scrollview)
    ScrollView scrollView;

    @BindView(R.id.about_subcontainer)
    LinearLayout linearLayout;
    private Unbinder unbinder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            // pre KK ScrollViews don't automatically save/restore their scroll state
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final int scrollviewPosition = savedInstanceState.getInt(KEY_SCROLLVIEW_POSITION, 0);

                if (scrollviewPosition != 0) {
                    // needs to be put on the queue so it executes when the view becomes visible
                    scrollView.post(new Runnable() {
                        public void run() {
                            scrollView.scrollTo(0, scrollviewPosition);
                        }
                    });
                }
            }
        }

        LayoutTransition layoutTransition = linearLayout.getLayoutTransition();
        // make LicensePanelView animations look nice
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        // stop settings from bubbling up to ScrollView to prevent scroll animation
        // on state restore and panel close
        layoutTransition.setAnimateParentHierarchy(false);
        linearLayout.setLayoutTransition(layoutTransition);

        facebookButton.setOnClickListener(new URLOpenOnActivityOnClickListener("https://www.facebook.com/criticalmaps"));
        twitterButton.setOnClickListener(new URLOpenOnActivityOnClickListener("https://twitter.com/CriticalMaps"));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            outState.putInt(KEY_SCROLLVIEW_POSITION, scrollView.getScrollY());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
