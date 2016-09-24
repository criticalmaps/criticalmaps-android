package de.stephanlindauer.criticalmaps.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ImageUtils {

    private ImageUtils() {}

    public static Bitmap rotateBitmap(File photoFile) {
        Bitmap sourceBitmap = BitmapFactory.decodeFile(photoFile.getPath());

        String orientString = null;
        try {
            ExifInterface exif = new ExifInterface(photoFile.getPath());
            orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = Integer.parseInt(orientString);
        int rotationAngle = 0;

        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            rotationAngle = 90;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            rotationAngle = 180;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            rotationAngle = 270;
        }

        if (rotationAngle == 0) {
            return sourceBitmap;
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle);

        Bitmap rotatedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(),
                sourceBitmap.getHeight(), matrix, true);
        sourceBitmap.recycle();

        return rotatedBitmap;
    }

    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
        }

        return image;
    }


    public static File getNewOutputImageFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "CriticalMaps");

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            return null;
        }

        final String id = UUID.randomUUID().toString().replace("-", "");
        return new File(mediaStorageDir.getPath() + File.separator + id + ".jpg");
    }
}
