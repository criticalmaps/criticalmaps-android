package de.stephanlindauer.criticalmaps.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.events.NewOverlayConfigEvent;
import de.stephanlindauer.criticalmaps.handler.ApplicationCloseHandler;
import de.stephanlindauer.criticalmaps.handler.ProcessCameraResultHandler;
import de.stephanlindauer.criticalmaps.handler.StartCameraHandler;
import de.stephanlindauer.criticalmaps.helper.clientinfo.BuildInfo;
import de.stephanlindauer.criticalmaps.helper.clientinfo.DeviceInformation;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;

public class SuperFragment extends Fragment {

    private File newCameraOutputFile;

    protected boolean shouldShowSternfahrtRoutes = false;

    private final EventBusProvider eventService = EventBusProvider.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_buttons, menu);
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
            default:
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getActivity(), R.string.something_went_wrong, Toast.LENGTH_LONG).show();
            return;
        }

        if (requestCode == RequestCodes.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            new ProcessCameraResultHandler(getActivity(), newCameraOutputFile).execute();
        }
    }

    private void startFeedbackIntent() {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL, new String[]{"stephanlindauer@posteo.de"});
        Email.putExtra(Intent.EXTRA_SUBJECT, "feedback critical maps");
        Email.putExtra(Intent.EXTRA_TEXT, DeviceInformation.getString() + BuildInfo.getString(getActivity().getPackageManager(), getActivity().getPackageName()));
        startActivity(Intent.createChooser(Email, "Send Feedback:"));
    }

    private void startDatenschutzIntent() {
        String url = "http://criticalmaps.net/datenschutzerklaerung.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void handleShowSternfahrt(MenuItem item) {
        item.setChecked(!item.isChecked());
        shouldShowSternfahrtRoutes = item.isChecked();
        eventService.post(new NewOverlayConfigEvent());
    }

    public void handleCloseRequested() {
        new ApplicationCloseHandler(getActivity()).execute();
    }

    public void setNewCameraOutputFile(File newCameraOutputFile) {
        this.newCameraOutputFile = newCameraOutputFile;
    }
}

