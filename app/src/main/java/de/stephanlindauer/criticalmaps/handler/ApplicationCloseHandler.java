package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.service.ServerSyncService;

public class ApplicationCloseHandler {

    private final Activity activity;

    public ApplicationCloseHandler(Activity activity) {
        this.activity = activity;
    }

    public void execute() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    ServerSyncService.stopService();
                    activity.finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.cancel();
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
        builder.setMessage(R.string.close).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }
}
