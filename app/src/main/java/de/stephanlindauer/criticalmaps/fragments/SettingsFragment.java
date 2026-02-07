package de.stephanlindauer.criticalmaps.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.databinding.FragmentSettingsBinding;
import de.stephanlindauer.criticalmaps.handler.ChooseGpxFileHandler;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import de.stephanlindauer.criticalmaps.model.StorageLocation;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;
import info.metadude.android.typedpreferences.BooleanPreference;
import info.metadude.android.typedpreferences.StringPreference;
import timber.log.Timber;

import static de.stephanlindauer.criticalmaps.utils.GpxUtils.persistPermissionOnFile;

import org.maplibre.android.offline.OfflineManager;

public class SettingsFragment extends Fragment {
    @Inject
    SharedPreferences sharedPreferences;

    private FragmentSettingsBinding binding;

    private StorageLocation storageLocation;

    @Inject
    App app;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        App.components().inject(this);
        storageLocation = new StorageLocation(app.getFilesDir());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        updateClearCachePref();
        updateStorageGraph();
        updateGpxFileName();

        binding.settingsShowOnLockscreenCheckbox.setChecked(
                new BooleanPreference(sharedPreferences, SharedPrefsKeys.SHOW_ON_LOCKSCREEN).get());

        binding.settingsKeepScreenOnCheckbox.setChecked(
                new BooleanPreference(sharedPreferences, SharedPrefsKeys.KEEP_SCREEN_ON).get());

        binding.settingsShowGpxCheckbox.setChecked(
                new BooleanPreference(sharedPreferences, SharedPrefsKeys.SHOW_GPX).get());

        binding.settingsClearCacheButton.setOnClickListener(v -> handleClearCacheClicked());

        binding.settingsShowOnLockscreenCheckbox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> handleShowOnLockscreenChecked(isChecked));
        binding.settingsKeepScreenOnCheckbox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> handleKeepScreenOnChecked(isChecked));

        binding.settingsShowGpxCheckbox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> handleShowTrack(isChecked));
        binding.settingsChooseGpxContainer.setOnClickListener(v -> handleChooseTrackClicked());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.CHOOSE_GPX_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();
            if (fileUri == null) {
                return;
            }
            String gpxFile = fileUri.toString();
            new StringPreference(
                    sharedPreferences, SharedPrefsKeys.GPX_FILE).set(gpxFile);
            persistPermissionOnFile(data, app.getContentResolver());
            updateGpxFileName();
        }
    }

    private void updateStorageGraph() {
        float usedPercentage =
                (float) storageLocation.getUsedSpace() / storageLocation.getTotalSizeBytes();
        long tileSize = storageLocation.getCacheSize();
        float tilePercentage = (float) tileSize / storageLocation.getTotalSizeBytes();

        binding.settingsCacheUsedSpaceText.setText(String.format(getString(R.string.settings_cache_used_mb),
                Formatter.formatShortFileSize(getActivity(), storageLocation.getUsedSpace())));
        binding.settingsCacheUsedCacheSpaceText.setText(String.format(getString(R.string.settings_cache_cache_mb),
                Formatter.formatShortFileSize(getActivity(), tileSize)));
        binding.settingsCacheFreeSpaceText.setText(String.format(getString(R.string.settings_cache_free_mb),
                Formatter.formatShortFileSize(getActivity(), storageLocation.getFreeSpaceBytes())));

        binding.settingsCacheStoragespacegraph.setBarPercentagesAnimated(
                usedPercentage, tilePercentage);
    }

    private void updateClearCachePref() {
        long currentSize = storageLocation.getCacheSize();
        Timber.d("Current cache size: %s",
                Formatter.formatShortFileSize(getActivity(), currentSize));
        binding.settingsClearCacheSummaryText.setText(
                String.format(getString(R.string.settings_cache_currently_used),
                        Formatter.formatShortFileSize(getActivity(), currentSize)));
    }

    @SuppressLint("Range")
    private void updateGpxFileName() {
        String gpxFile = new StringPreference(
                sharedPreferences, SharedPrefsKeys.GPX_FILE).get();
        String filename = gpxFile;

        Cursor fileCursor = getContext().getContentResolver().query(Uri.parse(gpxFile), null, null, null);
        if (fileCursor != null) {
            fileCursor.moveToFirst();
            filename = fileCursor.getString(fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            fileCursor.close();
        }

        binding.settingsChooseGpxSummaryText.setText(filename);
    }

    void handleClearCacheClicked() {
        OfflineManager.getInstance(getActivity()).clearAmbientCache(new OfflineManager.FileSourceCallback() {
            @Override
            public void onSuccess() {
                updateClearCachePref();
                updateStorageGraph();
            }

            @Override
            public void onError(@NonNull String s) {
                Timber.e("Error clearing cache: %s", s);
            }
        });

    }

    void handleShowOnLockscreenChecked(boolean isChecked) {
        new BooleanPreference(
                sharedPreferences, SharedPrefsKeys.SHOW_ON_LOCKSCREEN).set(isChecked);
    }

    void handleKeepScreenOnChecked(boolean isChecked) {
        new BooleanPreference(
                sharedPreferences, SharedPrefsKeys.KEEP_SCREEN_ON).set(isChecked);
    }

    void handleShowTrack(boolean isChecked) {
        new BooleanPreference(
                sharedPreferences, SharedPrefsKeys.SHOW_GPX).set(isChecked);
    }

    void handleChooseTrackClicked() {
        new ChooseGpxFileHandler(this).openChooser();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
