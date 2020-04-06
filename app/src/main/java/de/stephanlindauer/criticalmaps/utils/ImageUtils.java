package de.stephanlindauer.criticalmaps.utils;

import android.os.Environment;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.stephanlindauer.criticalmaps.App;
import timber.log.Timber;

public class ImageUtils {

    private ImageUtils() {
    }

    @Nullable
    public static File getNewCacheImageFile() {
        App app = App.components().app();
        File cacheDir = new File(app.getCacheDir(), "Pictures");
        return prepareImageFileFromBaseDir(cacheDir);
    }

    private static File prepareImageFileFromBaseDir(File baseDir) {
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            return null;
        }

        final String timestamp =
                new SimpleDateFormat("yyyyMMdd-HHmmssSSS", Locale.US).format(new Date());
        final String filename = "CriticalMaps-" + timestamp + ".jpg";
        return new File(baseDir, filename);
    }

    public static File movePhotoToPublicDir(File source) {
        File basePath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CriticalMaps");

        File destination = prepareImageFileFromBaseDir(basePath);

        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(destination).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (IOException e) {
            Timber.e(e, "Exception moving photo to public dir. Deleting temp file anyway.");
        } finally {
            //noinspection ResultOfMethodCallIgnored
            source.delete();
        }
        return destination;
    }
}
