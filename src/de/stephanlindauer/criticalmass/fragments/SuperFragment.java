package de.stephanlindauer.criticalmass.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import de.stephanlindauer.criticalmass.R;
import de.stephanlindauer.criticalmass.helper.LocationsPulling;
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
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar_buttons, menu);
        super.onCreateOptionsMenu(menu, inflater);

        trackingToggleButton = menu.findItem( R.id.settings_tracking_toggle );
        trackingToggleButton.setChecked(LocationsPulling.getInstance().isListeningForLocation());
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
            default:
                break;
        }
        return true;
    }

    private void handleTrackingToggled(MenuItem item) {
        item.setChecked(!item.isChecked());
        if (item.isChecked())
        {
            LocationsPulling.getInstance().shouldBeTrackingUsersLocation(true);
            showNoTrackingOverlay( false );
        }
        else {
            LocationsPulling.getInstance().shouldBeTrackingUsersLocation(false);
            showNoTrackingOverlay( true );
        }
    }

    private void showNoTrackingOverlay(boolean shouldShow ) {
        if ( noTrackingOverlay != null )
            noTrackingOverlay.setVisibility( shouldShow ? View.VISIBLE : View.INVISIBLE);
    }

    public void handleCloseRequested() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        TrackingInfoNotificationSetter.getInstance().cancel();
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

