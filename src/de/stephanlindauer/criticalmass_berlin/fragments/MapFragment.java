package de.stephanlindauer.criticalmass_berlin.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import de.stephanlindauer.criticalmass_berlin.R;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MapFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.relative_layout, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.layout.actionbar_buttons, menu);
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);
        FragmentActivity mContext = getActivity();

        final MapView mapView = new MapView(mContext, null);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        GeoPoint point = new GeoPoint((int) (52.520820 * 1E6), (int) (13.409346 * 1E6));
        mapView.getController().setCenter(point);
        mapView.getController().setZoom(11);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GeoPoint point = new GeoPoint((int) (52.520820 * 1E6), (int) (13.409346 * 1E6));
                mapView.getController().animateTo(point);
            }
        }, 200);

        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        RelativeLayout RL = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);
        RL.addView(mapView);
    }
}