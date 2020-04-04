package de.stephanlindauer.criticalmaps.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class IntentUtil {

    private IntentUtil() {
    }

    public static void startFromURL(final Context context, final String urlString) {
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(urlString));
        context.startActivity(i);
    }

    public static class URLOpenOnActivityOnClickListener implements View.OnClickListener {

        private final String url;

        public URLOpenOnActivityOnClickListener(final String url) {
            this.url = url;
        }

        @Override
        public void onClick(final View v) {
            startFromURL(v.getContext(), url);
        }
    }

}
