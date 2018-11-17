package de.stephanlindauer.criticalmaps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import butterknife.BindView;
import java.io.File;

import javax.inject.Inject;

import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.handler.ApplicationCloseHandler;
import de.stephanlindauer.criticalmaps.handler.PermissionCheckHandler;
import de.stephanlindauer.criticalmaps.handler.PrerequisitesChecker;
import de.stephanlindauer.criticalmaps.handler.ProcessCameraResultHandler;
import de.stephanlindauer.criticalmaps.handler.StartCameraHandler;
import de.stephanlindauer.criticalmaps.helper.clientinfo.BuildInfo;
import de.stephanlindauer.criticalmaps.helper.clientinfo.DeviceInformation;
import de.stephanlindauer.criticalmaps.provider.FragmentProvider;
import de.stephanlindauer.criticalmaps.service.ServerSyncService;
import de.stephanlindauer.criticalmaps.utils.DrawerClosingDrawerLayoutListener;
import de.stephanlindauer.criticalmaps.utils.ImageUtils;
import de.stephanlindauer.criticalmaps.utils.IntentUtil;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;
import timber.log.Timber;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final static String KEY_NAVID = "main_navid";
    private final static String KEY_SAVEDFRAGMENTSTATES = "main_savedfragmentstate";

    private File newCameraOutputFile;
    private int currentNavId;
    private SparseArray<Fragment.SavedState> savedFragmentStates = new SparseArray<>();

    @Inject
    public PermissionCheckHandler permissionCheckHandler;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.drawer_navigation)
    NavigationView drawerNavigation;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        drawerNavigation.setNavigationItemSelectedListener(this);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);

        drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        drawerLayout.addDrawerListener(new DrawerClosingDrawerLayoutListener());

        if (savedInstanceState != null) {
            SparseArray<Fragment.SavedState> restoredStates = savedInstanceState.getSparseParcelableArray(KEY_SAVEDFRAGMENTSTATES);
            if (restoredStates != null) {
                savedFragmentStates = restoredStates;
            }

            currentNavId = savedInstanceState.getInt(KEY_NAVID);
        } else {
            navigateTo(R.id.navigation_map);
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme); // has to be before super!

        super.onCreate(bundle);

        App.components().inject(this);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        new PrerequisitesChecker(this).showIntroductionIfNotShownBefore();

        ServerSyncService.startService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        permissionCheckHandler.attachActivity(this);
    }

    @Override
    protected void onStop() {
        permissionCheckHandler.detachActivity();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_buttons, menu);
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

    private void handleCloseRequested() {
        new ApplicationCloseHandler(this).execute();
    }

    private void startFeedbackIntent() {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL, new String[]{"stephanlindauer@posteo.de"});
        Email.putExtra(Intent.EXTRA_SUBJECT, "feedback critical maps");
        Email.putExtra(Intent.EXTRA_TEXT, DeviceInformation.getString() + BuildInfo.getString());
        startActivity(Intent.createChooser(Email, "Send Feedback:"));
    }

    private void startDatenschutzIntent() {
        IntentUtil.startFromURL(this, "http://criticalmaps.net/info#Datenschutzerklärung");
    }

    private void startRateTheApp() {
        IntentUtil.startFromURL(this, "https://play.google.com/store/apps/details?id=de.stephanlindauer.criticalmaps");
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
            File movedFile = ImageUtils.movePhotoToPublicDir(newCameraOutputFile);
            newCameraOutputFile = null;
            new ProcessCameraResultHandler(this, movedFile).execute();
        }
    }

    // TODO use URI instead of File and save it with state, otherwise rotation while loose the reference
    public void setNewCameraOutputFile(File newCameraOutputFile) {
        this.newCameraOutputFile = newCameraOutputFile;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("shouldClose") && intent.getBooleanExtra("shouldClose", false)) {
            new ApplicationCloseHandler(this).execute();
        }
    }

    @Override
    public void onAttachedToWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_NAVID, currentNavId);
        outState.putSparseParcelableArray(KEY_SAVEDFRAGMENTSTATES, savedFragmentStates);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
        navigateTo(item.getItemId());
        return true;
    }

    private void navigateTo(@IdRes int navId) {
        if (currentNavId == navId) {
            return; // no need for action
        }

        // save state of current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (currentFragment != null) {
            Fragment.SavedState state = getSupportFragmentManager().saveFragmentInstanceState(currentFragment);
            savedFragmentStates.put(currentNavId, state);
        }

        currentNavId = navId;
        final Fragment nextFragment = FragmentProvider.getFragmentForNavId(navId);

        // restore saved state of new fragment if it was shown before; otherwise passing null is ok
        nextFragment.setInitialSavedState(savedFragmentStates.get(navId));

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, nextFragment).commit();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!permissionCheckHandler.handlePermissionRequestCallback(requestCode, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
