package de.stephanlindauer.criticalmaps.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

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

    @BindView(R.id.settings_choose_storage_summary)
    TextView chooseStorageSummary;

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
        updateChooseStoragePref();
    }

    private void updateStorageGraph() {
        StorageLocationProvider.StorageLocation currentStorageLocation =
                storageLocationProvider.getActiveStorageLocation();

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
                storageLocationProvider.getActiveStorageLocation().getCacheSize() / (1000f*1000f);
        Timber.d("Current cache size: %.2f", currentSize);
        clearCacheSummary.setText(
                String.format(getString(R.string.settings_cache_currently_used), currentSize));
    }

    private void updateChooseStoragePref() {
        chooseStorageSummary.setText(storageLocationProvider.getActiveStorageLocation().displayName);
    }

    @OnClick(R.id.settings_clear_cache_button)
    void handleClearCacheClicked() {
        storageLocationProvider.getActiveStorageLocation().clearCache();
        updateClearCachePref();
        updateStorageGraph();
    }

    @OnClick(R.id.settings_choose_storage_container)
    void handleChooseStorageClicked() {
        ArrayList<StorageLocationProvider.StorageLocation> storageLocations =
                storageLocationProvider.getAllWritableStorageLocations();

        StorageLocationProvider.StorageLocation activeStorageLocation =
                storageLocationProvider.getActiveStorageLocation();

        int currentlyActive = 0;
        ArrayList<String> storageLocationNames = new ArrayList<>(4);

        for (int i = 0; i < storageLocations.size(); i++) {
            StorageLocationProvider.StorageLocation sL = storageLocations.get(i);
            storageLocationNames.add(sL.displayName + " " + String.format(
                    getString(R.string.settings_choose_storage_mb_free),
                    sL.freeSpace / (1000f*1000f)));

            if (storageLocations.get(i).storagePath.equals(activeStorageLocation.storagePath)) {
                currentlyActive = i;
            }
        }

        Activity activity = getActivity();
        int finalCurrentlyActive = currentlyActive;
        //noinspection ConstantConditions
        new AlertDialog.Builder(activity, R.style.AlertDialogTheme)
                .setTitle(R.string.settings_choose_storage_choose_title)
                .setSingleChoiceItems(
                        storageLocationNames.toArray(new String[0]), currentlyActive, null)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    int selectedStorage =
                            ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    if (selectedStorage == finalCurrentlyActive) {
                        return;
                    }

                    new AlertDialog.Builder(activity, R.style.AlertDialogTheme)
                            .setTitle(R.string.settings_choose_storage_confirm_title)
                            .setMessage(R.string.settings_choose_storage_confirm_message)
                            .setPositiveButton(R.string.settings_cache_clear, (dialog1, which) -> {
                                // clear old cache
                                activeStorageLocation.clearCache();
                                // set new storage
                                storageLocationProvider.setActiveStorageLocation(
                                        storageLocations.get(selectedStorage));
                                updateClearCachePref();
                                updateStorageGraph();
                                updateChooseStoragePref();
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create()
                            .show();

                })
                .create()
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
