package de.stephanlindauer.criticalmaps.utils;

import static de.stephanlindauer.criticalmaps.utils.AxtUtils.hideKeyBoard;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

public class DrawerClosingDrawerLayoutListener implements DrawerLayout.DrawerListener {

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        hideKeyBoard(drawerView);
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }
}
