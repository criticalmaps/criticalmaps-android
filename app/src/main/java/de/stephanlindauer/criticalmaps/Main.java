package de.stephanlindauer.criticalmaps;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import de.stephanlindauer.criticalmaps.adapter.TabsPagerAdapter;
import de.stephanlindauer.criticalmaps.events.NewOverlayConfigEvent;
import de.stephanlindauer.criticalmaps.handler.ApplicationCloseHandler;
import de.stephanlindauer.criticalmaps.handler.PrerequisitesChecker;
import de.stephanlindauer.criticalmaps.handler.ProcessCameraResultHandler;
import de.stephanlindauer.criticalmaps.handler.StartCameraHandler;
import de.stephanlindauer.criticalmaps.helper.CustomViewPager;
import de.stephanlindauer.criticalmaps.helper.clientinfo.BuildInfo;
import de.stephanlindauer.criticalmaps.helper.clientinfo.DeviceInformation;
import de.stephanlindauer.criticalmaps.model.SternfahrtModel;
import de.stephanlindauer.criticalmaps.model.UserModel;
import de.stephanlindauer.criticalmaps.notifications.trackinginfo.TrackingInfoNotificationSetter;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.service.LocationUpdatesService;
import de.stephanlindauer.criticalmaps.service.ServerSyncService;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;

public class Main extends FragmentActivity implements ActionBar.TabListener {

    //dependencies
    private final TrackingInfoNotificationSetter trackingInfoNotificationSetter = TrackingInfoNotificationSetter.getInstance();
    private final LocationUpdatesService locationUpdatesService = LocationUpdatesService.getInstance();
    private final UserModel userModel = UserModel.getInstance();
    private final EventBusProvider eventService = EventBusProvider.getInstance();
    private final SternfahrtModel sternfahrtModel = SternfahrtModel.getInstance();

    //misc
    private CustomViewPager viewPager;
    private File newCameraOutputFile;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_main);

        setupViewPager();

        new PrerequisitesChecker(this).execute();

        initializeNotifications();

        locationUpdatesService.initialize(getApplication());

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
            case R.id.show_sternfahrt:
                handleShowSternfahrt(item);
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

    public void handleCloseRequested() {
        new ApplicationCloseHandler(this).execute();
    }

    private void handleShowSternfahrt(MenuItem item) {
        item.setChecked(!item.isChecked());
        sternfahrtModel.shouldShowSternfahrtRoutes = item.isChecked();
        eventService.post(new NewOverlayConfigEvent());
    }

    private void startFeedbackIntent() {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL, new String[]{"stephanlindauer@posteo.de"});
        Email.putExtra(Intent.EXTRA_SUBJECT, "feedback critical maps");
        Email.putExtra(Intent.EXTRA_TEXT, DeviceInformation.getString() + BuildInfo.getString(this.getPackageManager(), this.getPackageName()));
        startActivity(Intent.createChooser(Email, "Send Feedback:"));
    }

    private void startDatenschutzIntent() {
        String url = "http://criticalmaps.net/datenschutzerklaerung.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void startRateTheApp() {
        String str = "https://play.google.com/store/apps/details?id=de.stephanlindauer.criticalmaps";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(str)));
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

    private void initializeNotifications() {
        trackingInfoNotificationSetter.initialize(getApplication());
        trackingInfoNotificationSetter.show();

        userModel.initialize(this);
    }

    private void setupViewPager() {
        viewPager = (CustomViewPager) findViewById(R.id.pager);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsPagerAdapter);

        actionBar.addTab(actionBar.newTab().setText(R.string.section_map).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_chat).setTabListener(this).setTag("chat_tab"));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_twitter).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_rules).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.section_about).setTabListener(this));

        registerListenersForSwipedChanges(actionBar);
    }

    private void registerListenersForSwipedChanges(final ActionBar actionBar) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    private void hideKeyBoard() {
        EditText editMessageTextfield = (EditText) findViewById(R.id.chat_edit_message);

        if (editMessageTextfield == null)
            return;

        editMessageTextfield.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editMessageTextfield.getWindowToken(), 0);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if ("chat_tab".equals(tab.getTag())) {
            hideKeyBoard();
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        //hack
    }

    @Override
    public void onAttachedToWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

}
