package de.stephanlindauer.criticalmaps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
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
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.service.LocationUpdatesService;
import de.stephanlindauer.criticalmaps.service.ServerSyncService;
import de.stephanlindauer.criticalmaps.utils.IntentUtil;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;
import java.io.File;

public class Main extends AppCompatActivity {

    //dependencies
    private final LocationUpdatesService locationUpdatesService = LocationUpdatesService.getInstance();
    private final UserModel userModel = UserModel.getInstance();
    private final EventBusProvider eventService = EventBusProvider.getInstance();
    private final SternfahrtModel sternfahrtModel = SternfahrtModel.getInstance();

    //misc
    private File newCameraOutputFile;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_main);

        setupTabs();

        new PrerequisitesChecker(this).execute();

        userModel.initialize(this);

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
        Email.putExtra(Intent.EXTRA_TEXT, DeviceInformation.getString() + BuildInfo.getString());
        startActivity(Intent.createChooser(Email, "Send Feedback:"));
    }

    private void startDatenschutzIntent() {
        IntentUtil.startFromURL(this, "http://criticalmaps.net/datenschutzerklaerung.html");
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

    private void setupTabs() {
        CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.pager);

        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getApplication(), getSupportFragmentManager());
        viewPager.setAdapter(tabsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabsFromPagerAdapter(tabsPagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            private void hideKeyBoard() {
                EditText editMessageTextfield = (EditText) findViewById(R.id.chat_edit_message);

                if (editMessageTextfield == null)
                    return;

                editMessageTextfield.clearFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(editMessageTextfield.getWindowToken(), 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);

                if (getResources().getString(R.string.section_chat).equals(tab.getText())) {
                    hideKeyBoard();
                }
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

}
