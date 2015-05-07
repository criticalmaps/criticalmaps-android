package de.stephanlindauer.criticalmaps.model;

import android.content.Context;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.RoadParser;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;

import java.util.ArrayList;

public class SternfahrtModel {

    private ArrayList<Polyline> allOverLays;

    public ArrayList<Polyline> getAllOverlays(Context context) {
        if (allOverLays != null) {
            return allOverLays;
        } else {
        allOverLays = new ArrayList<Polyline>();
            RoadManager roadManager = new OSRMRoadManager();
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_1_werder), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_2_brandenburg), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_3_nauen), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_4_heiligensee), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_5_frohnau), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_6_jungfernheide), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_7_oranienburg), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_8_wandlitzsee), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_9_eberswalde), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_10_ahrensfelde), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_11_strausberg), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_12_frankfurt), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_13_koenigswusterhausen), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_14_zossen), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_15_alttempelhof), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_16_bundesplatz), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_17_ludwigsfelde), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_18_potsdamrehbruecke), context));
            allOverLays.add(roadManager.buildRoadOverlay(RoadParser.getRoadFor(context, R.raw.sternfahrt_19_familien), context));
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
