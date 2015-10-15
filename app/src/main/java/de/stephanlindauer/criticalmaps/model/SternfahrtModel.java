package de.stephanlindauer.criticalmaps.model;

import android.content.Context;
import android.os.AsyncTask;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.RoadManager;

import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.events.NewOverlayConfigEvent;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.utils.RoadParser;

public class SternfahrtModel {

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
    public boolean shouldShowSternfahrtRoutes = false;
    private boolean shouldStartGenerating = true;

    //singleton
    private static SternfahrtModel instance;

    private SternfahrtModel() {
        allOverLays = new ArrayList<>(routeIDs.length);
    }

    public static SternfahrtModel getInstance() {
        if (SternfahrtModel.instance == null) {
            SternfahrtModel.instance = new SternfahrtModel();
        }
        return SternfahrtModel.instance;
    }

    public ArrayList<Polyline> getAllOverlays(Context context) {
        if (shouldStartGenerating) {
            shouldStartGenerating = false;
            new BuildOverlaysTask().execute(context);
        }

        return allOverLays;
    }

    private class BuildOverlaysTask extends AsyncTask<Context, Void, ArrayList<Polyline>> {

        @Override
        protected ArrayList<Polyline> doInBackground(Context... context) {
            // routeIDs is constant, so it's safe to use here
            ArrayList<Polyline> overlays = new ArrayList<>(routeIDs.length);
            for(int id : routeIDs) {
                overlays.add(RoadManager.buildRoadOverlay(RoadParser.getRoadFor(context[0], id), context[0]));
            }
            return overlays;
        }

        @Override
        protected void onPostExecute(ArrayList<Polyline> result) {
            allOverLays = result;
            EventBusProvider.getInstance().post(new NewOverlayConfigEvent());
        }
    }
}
