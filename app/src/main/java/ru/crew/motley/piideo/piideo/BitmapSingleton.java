package ru.crew.motley.piideo.piideo;

import android.graphics.Bitmap;

/**
 * Created by vas on 1/4/18.
 */

public class BitmapSingleton {

    private static Bitmap fromPhotoToView;

    public static void save(Bitmap cache) {
        fromPhotoToView = cache;
    }

    public static void clear() {
        fromPhotoToView.recycle();
        fromPhotoToView = null;
    }

    public static Bitmap image() {
        return fromPhotoToView;
    }
}
