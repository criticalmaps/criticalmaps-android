package de.stephanlindauer.criticalmaps.model;

import android.content.Context;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.RoadManager;

import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.RoadParser;

public class SternfahrtModel {

    public boolean shouldShowSternfahrtRoutes = false;

    private static final int[] routeIDs = {
            R.raw.sternfahrt_1_werder,
            R.raw.sternfahrt_2_brandenburg,
            R.raw.sternfahrt_3_nauen,
            R.raw.sternfahrt_4_heiligensee,
            R.raw.sternfahrt_5_frohnau,
            R.raw.sternfahrt_6_jungfernheide,
            R.raw.sternfahrt_7_oranienburg,
            R.raw.sternfahrt_8_wandlitzsee,
            R.raw.sternfahrt_9_eberswalde,
            R.raw.sternfahrt_10_ahrensfelde,
            R.raw.sternfahrt_11_strausberg,
            R.raw.sternfahrt_12_frankfurt,
            R.raw.sternfahrt_13_koenigswusterhausen,
            R.raw.sternfahrt_14_zossen,
            R.raw.sternfahrt_15_alttempelhof,
            R.raw.sternfahrt_16_bundesplatz,
            R.raw.sternfahrt_17_ludwigsfelde,
            R.raw.sternfahrt_18_potsdamrehbruecke,
            R.raw.sternfahrt_19_familien };

    private ArrayList<Polyline> allOverLays;

    public ArrayList<Polyline> getAllOverlays(Context context) {
        if (allOverLays != null) {
            return allOverLays;
        } else {
            allOverLays = new ArrayList<>(routeIDs.length);

            for(int id : routeIDs) {
                allOverLays.add(RoadManager.buildRoadOverlay(RoadParser.getRoadFor(context, id), context));
            }

            return allOverLays;
        }
    }

    //singleton
    private static SternfahrtModel instance;

    public static SternfahrtModel getInstance() {
        if (SternfahrtModel.instance == null) {
            SternfahrtModel.instance = new SternfahrtModel();
        }
        return SternfahrtModel.instance;
    }

}
