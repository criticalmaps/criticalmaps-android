package de.stephanlindauer.criticalmaps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.handler.ApplicationCloseHandler;
import de.stephanlindauer.criticalmaps.handler.PrerequisitesChecker;
import de.stephanlindauer.criticalmaps.handler.ProcessCameraResultHandler;
import de.stephanlindauer.criticalmaps.handler.StartCameraHandler;
import de.stephanlindauer.criticalmaps.helper.clientinfo.BuildInfo;
import de.stephanlindauer.criticalmaps.helper.clientinfo.DeviceInformation;
import de.stephanlindauer.criticalmaps.model.UserModel;
import de.stephanlindauer.criticalmaps.provider.FragmentProvider;
import de.stephanlindauer.criticalmaps.service.ServerSyncService;
import de.stephanlindauer.criticalmaps.utils.DrawerClosingDrawerLayoutListener;
import de.stephanlindauer.criticalmaps.utils.IntentUtil;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Inject
    UserModel userModel;

    //misc
    private File newCameraOutputFile;
    private int currentNavId;
    private final SimpleArrayMap<Integer, Fragment.SavedState> savedFragmentStates = new SimpleArrayMap<>();

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Bind(R.id.drawer_navigation)
    NavigationView drawerNavigation;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        drawerNavigation.setNavigationItemSelectedListener(this);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);

        drawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        drawerLayout.setDrawerListener(new DrawerClosingDrawerLayoutListener());
        navigateTo(R.id.navigation_map);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        App.components().inject(this);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        new PrerequisitesChecker(this).execute();

        userModel.initialize(this);

        startSyncService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_close:
                handleCloseRequested();
                break;
            case R.id.take_picture:
                new StartCameraHandler(this).execute();
                break;
            case R.id.settings_feedback:
                startFeedbackIntent();
                break;
            case R.id.settings_datenschutz:
                startDatenschutzIntent();
                break;
            case R.id.rate_the_app:
                startRateTheApp();
            default:
                break;
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
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
            return;
        }

        if (requestCode == RequestCodes.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            new ProcessCameraResultHandler(this, newCameraOutputFile).execute();
        }
    }

    public void setNewCameraOutputFile(File newCameraOutputFile) {
        this.newCameraOutputFile = newCameraOutputFile;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra("shouldClose") && intent.getBooleanExtra("shouldClose", false)) {
            new ApplicationCloseHandler(this).execute();
        }
        super.onNewIntent(intent);
    }

    private void startSyncService() {
        Intent syncServiceIntent = new Intent(this, ServerSyncService.class);
        startService(syncServiceIntent);
    }

    @Override
    public void onAttachedToWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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

        // restore saved state of new fragment if it was shown before
        if (savedFragmentStates.containsKey(navId)) {
            nextFragment.setInitialSavedState(savedFragmentStates.get(navId));
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, nextFragment).commit();
    }
}
