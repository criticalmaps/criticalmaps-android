package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.service.ServerSyncService;

public class ApplicationCloseHandler {

    private final Activity activity;

    public ApplicationCloseHandler(Activity activity) {
        this.activity = activity;
    }

    public void execute() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        ServerSyncService.stopService();
                        activity.finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
        builder.setMessage(R.string.close).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }
}
