package de.stephanlindauer.criticalmaps;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewGroupCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.databinding.ActivityMainBinding;
import de.stephanlindauer.criticalmaps.handler.ApplicationCloseHandler;
import de.stephanlindauer.criticalmaps.handler.PermissionCheckHandler;
import de.stephanlindauer.criticalmaps.handler.ProcessCameraResultHandler;
import de.stephanlindauer.criticalmaps.handler.StartCameraHandler;
import de.stephanlindauer.criticalmaps.helper.clientinfo.BuildInfo;
import de.stephanlindauer.criticalmaps.helper.clientinfo.DeviceInformation;
import de.stephanlindauer.criticalmaps.managers.LocationUpdateManager;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import de.stephanlindauer.criticalmaps.provider.FragmentProvider;
import de.stephanlindauer.criticalmaps.service.ServerSyncService;
import de.stephanlindauer.criticalmaps.utils.DrawerClosingDrawerLayoutListener;
import de.stephanlindauer.criticalmaps.utils.ImageUtils;
import de.stephanlindauer.criticalmaps.utils.IntentUtil;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;
import info.metadude.android.typedpreferences.BooleanPreference;
import timber.log.Timber;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final static String KEY_NAV_ID = "main_navid";
    private final static String KEY_SAVED_FRAGMENT_STATES = "main_savedfragmentstate";
    private final static String KEY_NEW_CAMERA_OUTPUT_FILE = "main_newcameraoutputfile";

    @Inject
    public PermissionCheckHandler permissionCheckHandler;

    @Inject
    LocationUpdateManager locationUpdateManager;

    @Inject
    SharedPreferences sharedPreferences;
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener =
            (sharedPreferences, key) -> {
                switch (key) {
                    case SharedPrefsKeys.SHOW_ON_LOCKSCREEN:
                        setShowOnLockscreen();
                        break;
                    case SharedPrefsKeys.KEEP_SCREEN_ON:
                        setKeepScreenOn();
                        break;
                    case SharedPrefsKeys.PRIVACY_POLICY_ACCEPTED:
                        if (!LocationUpdateManager.checkPermission()) {
                            locationUpdateManager.requestPermission();
                        }
                        break;
                }
            };

    private ActivityMainBinding binding;

    private Uri newCameraOutputFile;
    private int currentNavId;
    private SparseArray<Fragment.SavedState> savedFragmentStates = new SparseArray<>();
    private SwitchCompat observerModeSwitch;
    private BooleanPreference privacyPolicyAcceptedPreference;

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme); // has to be before super!
        super.onCreate(bundle);

        App.components().inject(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // Setup windows inset handling
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setStatusBarContrastEnforced(false);
            getWindow().setNavigationBarContrastEnforced(false);
        }

        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        ViewGroupCompat.installCompatInsetsDispatch(binding.getRoot());

        insetsController.setAppearanceLightStatusBars(true);
        insetsController.setAppearanceLightNavigationBars(true);

        setContentView(binding.getRoot());

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawers();
                } else if (currentNavId != R.id.navigation_map) {
                    navigateTo(R.id.navigation_map);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Apply window insets
        View navHeader = binding.drawerNavigation.getHeaderView(0);
        final int originalNavHeaderPaddingTop = navHeader.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerNavigation, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply insets to toolbar
            int toolbarMargin = currentNavId == R.id.navigation_map ? getResources().getDimensionPixelSize(R.dimen.map_toolbar_margins) : 0;
            ViewGroup.MarginLayoutParams lpToolbar =
                    (ViewGroup.MarginLayoutParams) binding.toolbar.getLayoutParams();

            lpToolbar.topMargin = insets.top + toolbarMargin;
            lpToolbar.leftMargin = insets.left + toolbarMargin;
            lpToolbar.rightMargin = insets.right + toolbarMargin;
            binding.toolbar.setLayoutParams(lpToolbar);

            // Apply insets to the NavigationView
            View navView = binding.drawerNavigation.findViewById(com.google.android.material.R.id.design_navigation_view);
            navView.setPadding(
                    insets.left, // navbar horizontal
                    navView.getPaddingTop(),
                    navView.getPaddingEnd(),
                    insets.bottom);

            navHeader.setPadding(
                    navHeader.getPaddingStart(),
                    originalNavHeaderPaddingTop + insets.top,
                    navHeader.getPaddingEnd(),
                    navHeader.getPaddingBottom());

            return WindowInsetsCompat.CONSUMED;
        });

        setShowOnLockscreen();
        setKeepScreenOn();
    }

    @Override
    protected void onStart() {
        super.onStart();
        permissionCheckHandler.attachActivity(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        privacyPolicyAcceptedPreference = new BooleanPreference(sharedPreferences, SharedPrefsKeys.PRIVACY_POLICY_ACCEPTED);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        binding.drawerNavigation.setNavigationItemSelectedListener(this);

        setSupportActionBar(binding.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, R.string.open_drawer, R.string.close_drawer);

        binding.drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        binding.drawerLayout.addDrawerListener(new DrawerClosingDrawerLayoutListener() {
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                super.onDrawerClosed(drawerView);
                navigateTo(binding.drawerNavigation.getCheckedItem().getItemId());
            }
        });

        observerModeSwitch = binding.drawerNavigation.getMenu().findItem(R.id.navigation_observer_mode)
                .getActionView().findViewById(R.id.navigation_observer_mode_switch);
        observerModeSwitch.setChecked(new BooleanPreference(
                sharedPreferences, SharedPrefsKeys.OBSERVER_MODE_ACTIVE).get());
        observerModeSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> handleObserverModeSwitchCheckedChanged(isChecked));

        binding.understandButton.setOnClickListener(view -> {
            binding.introductionView.setVisibility(View.GONE);
            privacyPolicyAcceptedPreference.set(true);
        });

        if (savedInstanceState != null) {
            SparseArray<Fragment.SavedState> restoredStates =
                    savedInstanceState.getSparseParcelableArray(KEY_SAVED_FRAGMENT_STATES);
            if (restoredStates != null) {
                savedFragmentStates = restoredStates;
            }

            newCameraOutputFile = savedInstanceState.getParcelable(KEY_NEW_CAMERA_OUTPUT_FILE);

            currentNavId = savedInstanceState.getInt(KEY_NAV_ID);
            if (currentNavId != R.id.navigation_map) {
                // set toolbar title
                //noinspection ConstantConditions
                getSupportActionBar().setTitle(binding.drawerNavigation.getCheckedItem().getTitle());

                // set toolbar margins
                ViewGroup.MarginLayoutParams toolbarParams =
                        (ViewGroup.MarginLayoutParams) binding.toolbar.getLayoutParams();
                int marginPixels =
                        getResources().getDimensionPixelSize(R.dimen.map_toolbar_margins);

                toolbarParams.topMargin -= marginPixels;
                toolbarParams.rightMargin -= marginPixels;
                toolbarParams.leftMargin -= marginPixels;
                binding.toolbar.setLayoutParams(toolbarParams);

                // set toolbar background
                ((GradientDrawable) binding.toolbar.getBackground()).setCornerRadius(0F);
            }
        } else {
            navigateTo(R.id.navigation_map);
        }

        locationUpdateManager.initialize();

        final boolean isPrivacyPolicyAccepted = privacyPolicyAcceptedPreference.get();
        if (!isPrivacyPolicyAccepted) {
            binding.introductionText.setMovementMethod(LinkMovementMethod.getInstance());
            binding.introductionText.setText(Html.fromHtml(getString(R.string.introduction_gps), Html.FROM_HTML_MODE_LEGACY));
            binding.introductionView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume() called");
        final boolean isPrivacyPolicyAccepted = privacyPolicyAcceptedPreference.get();
        if (isPrivacyPolicyAccepted) {
            initiateServiceStartIfPermitted();
        }
    }

    @Override
    protected void onStop() {
        permissionCheckHandler.detachActivity();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(
                sharedPreferenceChangeListener);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_close) {
            handleCloseRequested();
        } else if (itemId == R.id.take_picture) {
            new StartCameraHandler(this).execute();
        } else if (itemId == R.id.settings_feedback) {
            startFeedbackIntent();
        } else if (itemId == R.id.settings_datenschutz) {
            startDatenschutzIntent();
        } else if (itemId == R.id.rate_the_app) {
            startRateTheApp();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void handleCloseRequested() {
        new ApplicationCloseHandler(this).execute();
    }

    private void startFeedbackIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/email")
                .putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.contact_email)})
                .putExtra(Intent.EXTRA_SUBJECT, "feedback critical maps")
                .putExtra(Intent.EXTRA_TEXT, DeviceInformation.getString() + BuildInfo.getString());
        startActivity(Intent.createChooser(intent, "Send Feedback:"));
    }

    private void startDatenschutzIntent() {
        IntentUtil.startFromURL(this,
                "http://criticalmaps.net/info#Datenschutzerklärung");
    }

    private void startRateTheApp() {
        IntentUtil.startFromURL(this,
                "https://play.google.com/store/apps/details?id=de.stephanlindauer.criticalmaps");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            Timber.d("requestCode: %d, resultCode: %d", requestCode, resultCode);
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
            return;
        }

        if (requestCode == RequestCodes.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            File movedFile =
                    ImageUtils.movePhotoToFilesDir(new File(newCameraOutputFile.getPath()));
            newCameraOutputFile = null;
            new ProcessCameraResultHandler(this, movedFile).execute();
        }
    }

    public void setNewCameraOutputFile(Uri newCameraOutputFile) {
        this.newCameraOutputFile = newCameraOutputFile;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("shouldClose") && intent.getBooleanExtra("shouldClose", false)) {
            new ApplicationCloseHandler(this).execute();
        }
    }

    private void setShowOnLockscreen() {
        if (new BooleanPreference(sharedPreferences, SharedPrefsKeys.SHOW_ON_LOCKSCREEN).get()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
    }

    private void setKeepScreenOn() {
        if (new BooleanPreference(sharedPreferences, SharedPrefsKeys.KEEP_SCREEN_ON).get()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_NAV_ID, currentNavId);
        outState.putSparseParcelableArray(KEY_SAVED_FRAGMENT_STATES, savedFragmentStates);
        if (newCameraOutputFile != null) {
            outState.putParcelable(KEY_NEW_CAMERA_OUTPUT_FILE, newCameraOutputFile);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == R.id.navigation_group) {
            item.setChecked(true);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            //noinspection ConstantConditions
            getSupportActionBar().setTitle(item.getTitle());
            return true;
        }
        if (item.getItemId() == R.id.navigation_observer_mode) {
            observerModeSwitch.setChecked(!observerModeSwitch.isChecked());
            return true;
        }
        return false;
    }

    private void navigateTo(@IdRes int navId) {
        if (currentNavId == navId) {
            return; // no need for action
        }

        // save state of current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (currentFragment != null) {
            Fragment.SavedState state =
                    getSupportFragmentManager().saveFragmentInstanceState(currentFragment);
            savedFragmentStates.put(currentNavId, state);
        }

        final Fragment nextFragment = FragmentProvider.getFragmentForNavId(navId);

        // restore saved state of new fragment if it was shown before; otherwise passing null is ok
        nextFragment.setInitialSavedState(savedFragmentStates.get(navId));

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, nextFragment).commit();

        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        // animate toolbar and statusbar color
        if (currentNavId == R.id.navigation_map) {
            // from map to other fragment
            animateToolbar(200, false);
            insetsController.setAppearanceLightStatusBars(false);
            insetsController.setAppearanceLightNavigationBars(false);
        } else if (navId == R.id.navigation_map && currentNavId != 0) {
            // from other fragment to map AND not app start
            animateToolbar(500, true);
            insetsController.setAppearanceLightStatusBars(true);
            insetsController.setAppearanceLightNavigationBars(true);
        }

        currentNavId = navId;
    }

    private void animateToolbar(int durationMillis, boolean toMap) {
        ViewGroup.MarginLayoutParams toolbarParamsChanging =
                (ViewGroup.MarginLayoutParams) binding.toolbar.getLayoutParams();

        ViewGroup.MarginLayoutParams toolbarParamsStart =
                new ViewGroup.MarginLayoutParams(toolbarParamsChanging);

        int marginPixels = getResources().getDimensionPixelSize(R.dimen.map_toolbar_margins);

        if (toMap) {
            marginPixels *= -1;
        }

        ValueAnimator marginAnimator = ValueAnimator.ofInt(0, marginPixels);
        marginAnimator.setDuration(durationMillis);
        marginAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        marginAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            toolbarParamsChanging.topMargin = toolbarParamsStart.topMargin - animatedValue;
            toolbarParamsChanging.rightMargin = toolbarParamsStart.rightMargin - animatedValue;
            toolbarParamsChanging.leftMargin = toolbarParamsStart.leftMargin - animatedValue;
            binding.toolbar.setLayoutParams(toolbarParamsChanging);
        });

        GradientDrawable toolbarBackground = (GradientDrawable) binding.toolbar.getBackground();
        float radiusMap = getResources().getDimension(R.dimen.map_toolbar_corner_radius);
        float radiusFrom = toMap ? 0 : radiusMap;
        float radiusTo = toMap ? radiusMap : 0;

        ValueAnimator radiusAnimator = ValueAnimator.ofFloat(radiusFrom, radiusTo);
        radiusAnimator.setDuration(durationMillis);
        marginAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        radiusAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            toolbarBackground.setCornerRadius(value);
        });

        marginAnimator.start();
        radiusAnimator.start();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!permissionCheckHandler.handlePermissionRequestCallback(requestCode, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void handleObserverModeSwitchCheckedChanged(boolean isChecked) {
        new BooleanPreference(
                sharedPreferences, SharedPrefsKeys.OBSERVER_MODE_ACTIVE).set(isChecked);
    }

    private void initiateServiceStartIfPermitted() {
        if (LocationUpdateManager.checkPermission()) {
            if (!ServerSyncService.isCurrentlyRunning()) {
                Timber.d("Location and notification permissions granted. Attempting to start ServerSyncService.");
                ServerSyncService.startService();
            } else {
                Timber.d("Location and notification permission granted, but service is already running.");
            }
        } else {
            Timber.d("Location and notification permission NOT granted. ServerSyncService will not be started.");
        }
    }
}
