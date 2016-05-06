package de.stephanlindauer.criticalmaps.model;

import java.util.ArrayList;
import java.util.List;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.vo.RouteConfiguration;
import de.stephanlindauer.criticalmaps.vo.RoutesCategory;

public class RoutesModel {
    public final List<RoutesCategory> routesCategories = new ArrayList<RoutesCategory>() {
        {
            RoutesCategory sternfahrtCategory = new RoutesCategory("Sternfahrt");
            sternfahrtCategory.add(new RouteConfiguration("Ahrensfelde", R.raw.sternfahrt_ahrensfelde, sternfahrtCategory));
            sternfahrtCategory.add(new RouteConfiguration("Alt-Tempelhof", R.raw.sternfahrt_alttempelhof, sternfahrtCategory));
            add(sternfahrtCategory);

            RoutesCategory kreisfahrtCategory = new RoutesCategory("Kreisfahrt");
            kreisfahrtCategory.add(new RouteConfiguration("Bundesplatz", R.raw.sternfahrt_bundesplatz, kreisfahrtCategory));
            kreisfahrtCategory.add(new RouteConfiguration("Eberswalde", R.raw.sternfahrt_eberswalde, kreisfahrtCategory));
            add(kreisfahrtCategory);
        }
    };

    private RoutesModel() {
    }

    public static RoutesModel getInstance() {
        if (RoutesModel.instance == null) {
            RoutesModel.instance = new RoutesModel();
        }
        return RoutesModel.instance;
    }    //singleton

    private static RoutesModel instance;
}
