package ru.crew.motley.piideo.util;

import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;

/**
 * Created by vas on 2/28/18.
 */

public class ImageUtils {

    public static int orientation(String path) throws IOException {
        ExifInterface exifInterface = new ExifInterface(path);
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        int rotationDegrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegrees = 270;
                break;
        }
        return rotationDegrees;
    }

    public static int orientation(File file) throws IOException {
        return orientation(file.getAbsolutePath());
    }
}
