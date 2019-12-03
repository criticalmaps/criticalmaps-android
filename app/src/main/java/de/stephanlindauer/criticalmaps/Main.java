package de.stephanlindauer.criticalmaps;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import javax.inject.Inject;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
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
    SharedPreferences sharedPreferences;
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener =
            (sharedPreferences, key) -> {
                if (SharedPrefsKeys.SHOW_ON_LOCKSCREEN.equals(key)) {
                    setShowOnLockscreen();
                } else if (SharedPrefsKeys.KEEP_SCREEN_ON.equals(key)) {
                    setKeepScreenOn();
                }
            };
    @Inject
    LocationUpdateManager locationUpdateManager;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.drawer_navigation)
    NavigationView drawerNavigation;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.content_frame)
    FrameLayout contentFrame;

    @BindView(R.id.understand_button)
    Button understandButton;

    @BindView(R.id.introduction_view)
    View introductionView;

    private Uri newCameraOutputFile;
    private int currentNavId;
    private SparseArray<Fragment.SavedState> savedFragmentStates = new SparseArray<>();
    private SwitchCompat observerModeSwitch;
    private BooleanPreference introductionAlreadyShownPreference;

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme); // has to be before super!
        super.onCreate(bundle);

        App.components().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            // inset the toolbar down by the status bar height
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                ViewGroup.MarginLayoutParams lpToolbar =
                        (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                lpToolbar.topMargin += insets.getSystemWindowInsetTop();

                toolbar.setLayoutParams(lpToolbar);

                // clear this listener so insets aren't re-applied
                ViewCompat.setOnApplyWindowInsetsListener(toolbar, null);
                return insets;
            });

            // inset header in nav drawer down by the status bar height
            View navHeader = drawerNavigation.getHeaderView(0);
            ViewCompat.setOnApplyWindowInsetsListener(navHeader, (v, insets) -> {
                v.setPaddingRelative(
                        v.getPaddingStart(), v.getPaddingTop() + insets.getSystemWindowInsetTop(),
                        v.getPaddingEnd(), v.getPaddingBottom());

                // clear this listener so insets aren't re-applied
                ViewCompat.setOnApplyWindowInsetsListener(navHeader, null);
                return insets;
            });
        }

        // This is a little hacky and might break with a materialcomponents lib update
        RecyclerView navigationMenuView = findViewById(R.id.design_navigation_view);
        navigationMenuView.setNestedScrollingEnabled(false);

        setShowOnLockscreen();
        setKeepScreenOn();

        ServerSyncService.startService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        permissionCheckHandler.attachActivity(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(
                sharedPreferenceChangeListener);
        introductionAlreadyShownPreference = new BooleanPreference(sharedPreferences, SharedPrefsKeys.INTRODUCTION_ALREADY_SHOWN);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        drawerNavigation.setNavigationItemSelectedListener(this);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);

        drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        drawerLayout.addDrawerListener(new DrawerClosingDrawerLayoutListener() {
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                super.onDrawerClosed(drawerView);
                navigateTo(drawerNavigation.getCheckedItem().getItemId());
            }
        });

        observerModeSwitch = drawerNavigation.getMenu().findItem(R.id.navigation_observer_mode)
                .getActionView().findViewById(R.id.navigation_observer_mode_switch);
        observerModeSwitch.setChecked(new BooleanPreference(
                sharedPreferences, SharedPrefsKeys.OBSERVER_MODE_ACTIVE).get());
        observerModeSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> handleObserverModeSwitchCheckedChanged(isChecked));

        understandButton.setOnClickListener(view -> {
            introductionView.setVisibility(View.GONE);

            introductionAlreadyShownPreference.set(true);
            if (!locationUpdateManager.checkPermission()) {
                locationUpdateManager.requestPermission();
            }
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
                getSupportActionBar().setTitle(drawerNavigation.getCheckedItem().getTitle());

                // set toolbar margins
                ViewGroup.MarginLayoutParams toolbarParams =
                        (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                int marginPixels =
                        getResources().getDimensionPixelSize(R.dimen.map_toolbar_margins);

                toolbarParams.topMargin -= marginPixels;
                toolbarParams.rightMargin -= marginPixels;
                toolbarParams.leftMargin -= marginPixels;
                toolbar.setLayoutParams(toolbarParams);

                // set toolbar background
                ((GradientDrawable) toolbar.getBackground()).setCornerRadius(0F);

                // set statusbar color
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(
                            ContextCompat.getColor(this, R.color.main_statusbarcolor_others));
                }
            }
        } else {
            navigateTo(R.id.navigation_map);
        }

        final boolean shouldShowIntroduction =
                !introductionAlreadyShownPreference.isSet() ||
                        !introductionAlreadyShownPreference.get() ||
                        !locationUpdateManager.checkPermission();
        if (shouldShowIntroduction) {
            introductionView.setVisibility(View.VISIBLE);
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
        switch (item.getItemId()) {
            case R.id.action_close:
                handleCloseRequested();
                break;
            case R.id.take_picture:
                new StartCameraHandler(this, permissionCheckHandler).execute();
                break;
            case R.id.settings_feedback:
                startFeedbackIntent();
                break;
            case R.id.settings_datenschutz:
                startDatenschutzIntent();
                break;
            case R.id.rate_the_app:
                startRateTheApp();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
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
                    ImageUtils.movePhotoToPublicDir(new File(newCameraOutputFile.getPath()));
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
            drawerLayout.closeDrawer(GravityCompat.START);
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

        // animate toolbar and statusbar color
        if (currentNavId == R.id.navigation_map) {
            // from map to other fragment
            animateToolbar(200, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fadeInStatusBarColor(200, false);
            }
        } else if (navId == R.id.navigation_map && currentNavId != 0) {
            // from other fragment to map AND not app start
            animateToolbar(500, true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fadeInStatusBarColor(500, true);
            }
        }

        currentNavId = navId;
    }

    private void animateToolbar(int durationMillis, boolean toMap) {
        ViewGroup.MarginLayoutParams toolbarParamsChanging =
                (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();

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
            toolbar.setLayoutParams(toolbarParamsChanging);
        });

        GradientDrawable toolbarBackground = (GradientDrawable) toolbar.getBackground();
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void fadeInStatusBarColor(int duration, boolean toMap) {
        int colorMap = ContextCompat.getColor(this, R.color.main_statusbarcolor_map);
        int colorOthers = ContextCompat.getColor(this, R.color.main_statusbarcolor_others);
        int colorFrom = toMap ? colorOthers : colorMap;
        int colorTo = toMap ? colorMap : colorOthers;

        ValueAnimator valueAnimator =
                ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            getWindow().setStatusBarColor(color);
        });

        valueAnimator.start();
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
}
