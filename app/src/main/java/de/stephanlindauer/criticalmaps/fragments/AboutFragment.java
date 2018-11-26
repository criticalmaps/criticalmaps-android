package de.stephanlindauer.criticalmaps.fragments;

import android.animation.LayoutTransition;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.IntentUtil.URLOpenOnActivityOnClickListener;

public class AboutFragment extends Fragment {

    @BindView(R.id.about_facebook)
    ImageButton facebookButton;

    @BindView(R.id.about_twitter)
    ImageButton twitterButton;

    @BindView(R.id.about_scrollview)
    NestedScrollView scrollView;

    @BindView(R.id.about_subcontainer)
    LinearLayout subContainer;
    private Unbinder unbinder;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LayoutTransition layoutTransition = subContainer.getLayoutTransition();
        // make LicensePanelView animations look nice
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        // stop settings from bubbling up to ScrollView to prevent scroll animation
        // on state restore and panel close
        layoutTransition.setAnimateParentHierarchy(false);
        subContainer.setLayoutTransition(layoutTransition);

        facebookButton.setOnClickListener(
                new URLOpenOnActivityOnClickListener("https://www.facebook.com/criticalmaps"));
        twitterButton.setOnClickListener(
                new URLOpenOnActivityOnClickListener("https://twitter.com/CriticalMaps"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
