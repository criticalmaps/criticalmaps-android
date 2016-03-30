package de.stephanlindauer.criticalmaps.model;

import android.content.Context;
import android.os.AsyncTask;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.RoadManager;

import java.util.ArrayList;
import java.util.List;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.vo.RouteConfiguration;

public class RoutesModel {

    private static final List<RouteConfiguration> routeConfigurations = new ArrayList<RouteConfiguration>() {
        {
            add(new RouteConfiguration("Sternfahrt", "Ahrensfelde", R.raw.sternfahrt_ahrensfelde));
            add(new RouteConfiguration("Sternfahrt", "Alt-Tempelhof", R.raw.sternfahrt_alttempelhof));
        }
    };

    //singleton
    private static RoutesModel instance;

    private RoutesModel() {

    }

    public static RoutesModel getInstance() {
        if (RoutesModel.instance == null) {
            RoutesModel.instance = new RoutesModel();
        }
        return RoutesModel.instance;
    }
}
