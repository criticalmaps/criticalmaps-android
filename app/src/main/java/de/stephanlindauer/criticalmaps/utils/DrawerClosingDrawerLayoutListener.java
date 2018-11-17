package de.stephanlindauer.criticalmaps.utils;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.View;

import org.ligi.axt.AXT;

public class DrawerClosingDrawerLayoutListener implements DrawerLayout.DrawerListener {

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        AXT.at(drawerView).hideKeyBoard();
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
