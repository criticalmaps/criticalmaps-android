package de.stephanlindauer.criticalmass.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import de.stephanlindauer.criticalmass.R;
import de.stephanlindauer.criticalmass.service.ServerPuller;
import de.stephanlindauer.criticalmass.helper.clientinfo.BuildInfo;
import de.stephanlindauer.criticalmass.helper.clientinfo.DeviceInformation;
import de.stephanlindauer.criticalmass.notifications.trackinginfo.TrackingInfoNotificationSetter;

public class SuperFragment extends Fragment {

    protected MenuItem trackingToggleButton;
    protected static Button noTrackingOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar_buttons, menu);
        super.onCreateOptionsMenu(menu, inflater);
        trackingToggleButton = menu.findItem(R.id.settings_tracking_toggle);
        trackingToggleButton.setChecked(ServerPuller.getInstance().isListeningForLocation());

        menu.findItem(R.id.settings_tracking_toggle).setChecked(ServerPuller.getInstance().isListeningForLocation());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_close:
                handleCloseRequested();
                break;
            case R.id.settings_tracking_toggle:
                handleTrackingToggled(item);
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

    private void startFeedbackIntent() {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL, new String[]{"stephan.lindauer@gmail.com"});
        Email.putExtra(Intent.EXTRA_SUBJECT, "feedback critical mass app");
        Email.putExtra(Intent.EXTRA_TEXT, DeviceInformation.getString() + BuildInfo.getString(getActivity().getPackageManager(), getActivity().getPackageName()));
        startActivity(Intent.createChooser(Email, "Send Feedback:"));
    }

    private void startDatenschutzIntent() {
        String url = "http://criticalmass.stephanlindauer.de/datenschutzerklaerung.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    private void handleTrackingToggled(MenuItem item) {
        item.setChecked(!item.isChecked());
        if (item.isChecked()) {
            ServerPuller.getInstance().shouldBeTrackingUsersLocation(true);
            showNoTrackingOverlay(false);
        } else {
            ServerPuller.getInstance().shouldBeTrackingUsersLocation(false);
            showNoTrackingOverlay(true);
        }
    }

    private void showNoTrackingOverlay(boolean shouldShow) {
        if (noTrackingOverlay != null)
            noTrackingOverlay.setVisibility(shouldShow ? View.VISIBLE : View.INVISIBLE);
    }

    public void handleCloseRequested() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        TrackingInfoNotificationSetter.getInstance().cancel();
                        getActivity().finish();
                        System.exit(0);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.close).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }
}

