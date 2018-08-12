package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.provider.StorageLocationProvider;
import de.stephanlindauer.criticalmaps.views.StorageSpaceGraph;
import timber.log.Timber;

public class SettingsFragment extends Fragment {

    private Unbinder unbinder;

    @BindView(R.id.settings_storagegraph)
    StorageSpaceGraph storageSpaceGraph;

    @BindView(R.id.settings_clear_cache_summary)
    TextView clearCacheSummary;

    @BindView(R.id.settings_cache_used_mb)
    TextView usedSpace;

    @BindView(R.id.settings_cache_cache_mb)
    TextView cacheSpace;

    @BindView(R.id.settings_cache_free_mb)
    TextView freeSpace;

    @Inject
    StorageLocationProvider storageLocationProvider;

    @Override @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        App.components().inject(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        //noinspection ConstantConditions
        unbinder = ButterKnife.bind(this, getView());

        updateClearCachePref();
        updateStorageGraph();
    }

    private void updateStorageGraph() {
        StorageLocationProvider.StorageLocation currentStorageLocation =
                storageLocationProvider.getSavedStorageLocation();

        float usedPercentage =
                (float) currentStorageLocation.usedSpace / currentStorageLocation.totalSize;

        long tileSize = currentStorageLocation.getCacheSize();

        float tilePercentage = (float) tileSize / currentStorageLocation.totalSize;

        usedSpace.setText(String.format(getString(R.string.settings_cache_used_mb),
                currentStorageLocation.usedSpace / (1000f*1000f)));
        cacheSpace.setText(String.format(getString(R.string.settings_cache_cache_mb),
                tileSize / (1000f*1000f)));
        freeSpace.setText(String.format(getString(R.string.settings_cache_free_mb),
                currentStorageLocation.freeSpace / (1000f*1000f)));

        storageSpaceGraph.setBarPercentagesAnimated(usedPercentage, tilePercentage);
    }

    private void updateClearCachePref() {
        float currentSize =
                storageLocationProvider.getSavedStorageLocation().getCacheSize() / (1000f*1000f);
        Timber.d("Current cache size: %.2f", currentSize);
        clearCacheSummary.setText(
                String.format(getString(R.string.settings_cache_currently_used), currentSize));
    }

    @OnClick(R.id.settings_clear_cache_button)
    void handleClearCacheClicked() {
        storageLocationProvider.getSavedStorageLocation().clearCache();
        updateClearCachePref();
        updateStorageGraph();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
