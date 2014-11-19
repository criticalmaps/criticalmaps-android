package de.stephanlindauer.criticalmaps.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public enum ImageCache {

    shared;

    public static final String TAG = ImageCache.class.getSimpleName();

    public HashMap<String, Bitmap> images;
    public static final String imageCachePath = "/twitter/profile_images/";

    public void loadImage(@NotNull final Context context, @NotNull final String imageUrl, @NotNull final AsyncCallback cb) {

        // check url and filename
        final URL url;
        final String fileName;
        final String storagePath = context.getFilesDir().getParent() + imageCachePath;
        try {
            Log.v(TAG, "url: " + imageUrl);
            url = new URL(imageUrl);
            fileName = new File(url.getFile()).getName();

            // create profile dir if not exist
            final File dir = new File(storagePath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("Unable to create " + dir.getAbsolutePath());
            }
        } catch (IOException e) {
            cb.onException(e);
            return;
        }

        // 1) load from memory
        if (images == null)
            images = new HashMap<>();

        if (images.containsKey(fileName)) {
            cb.onComplete(images.get(fileName));
            return;
        }

        // 2) load from disk
        loadFromDisk(cb, url, fileName, storagePath);
    }

    private void loadFromDisk(@NotNull final AsyncCallback cb, @NotNull final URL url, @NotNull final String fileName, @NotNull final String storagePath) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... params) {
                try {
                    Log.v(TAG, "trying to load from disk");
                    final Bitmap bitmap = decodeSampleBitmapFromFile(storagePath + fileName);
                    if (bitmap == null)
                        throw new Exception("couldn't load from disk");
                    // 4.1) persist
                    images.put(fileName, bitmap);
                    Log.v(TAG, "trying to load from disk: success");
                    cb.onComplete(bitmap);
                } catch (final Exception e) {

                    Log.v(TAG, "trying to load from disk: failed");

                    // 3) load from url
                    loadFromUrlAsync(url, fileName, storagePath, cb);
                }
                return null;
            }
        }.execute();
    }

    private void loadFromUrlAsync(@NotNull final URL url, @NotNull final String fileName, @NotNull final String storagePath, @NotNull final AsyncCallback cb) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... params) {
                try {
                    Log.v(TAG, "download from url: start downloading");
                    final InputStream is = (InputStream) url.getContent();
                    final Bitmap bitmap = BitmapFactory.decodeStream(is);

                    Log.v(TAG, "download from url: download complete");

                    // 4.1) persist
                    images.put(fileName, bitmap);
                    Log.v(TAG, "download from url: memory cache complete");

                    // 4.2) persist even harder
                    Log.v(TAG, "download from url: start storing image at " + storagePath + fileName);
                    final FileOutputStream out = new FileOutputStream(storagePath + fileName);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

                    Log.v(TAG, "download from url: start storing image at " + storagePath + fileName + " completed");
                    cb.onComplete(bitmap);

                } catch (final IOException e) {
                    Log.e(TAG, "" + e.getMessage());
                    cb.onException(e);
                }
                return null;
            }

        }.execute();
    }

    public static Bitmap decodeSampleBitmapFromFile(@NotNull final String filePath) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }
}
