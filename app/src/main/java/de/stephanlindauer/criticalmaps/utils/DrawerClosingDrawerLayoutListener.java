package de.stephanlindauer.criticalmaps.utils;

import android.support.v4.widget.DrawerLayout;
import android.view.View;

import org.ligi.axt.AXT;

public class DrawerClosingDrawerLayoutListener implements DrawerLayout.DrawerListener {

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        AXT.at(drawerView).hideKeyBoard();
    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
