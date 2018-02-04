package ru.crew.motley.piideo.piideo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.BindView;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.ChatActivity;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.chat.db.PiideoRow;
import ru.crew.motley.piideo.piideo.service.Recorder;

public class PhotoImageFragment extends ButterFragment {

    private static final String TAG = PhotoImageFragment.class.getSimpleName();

    private static final String ARG_PIIDEO_NAME = "piideoName";
    private static final String ARG_FILTER_PATH = "filter";


    private String mPiideoName;
    private boolean mLongPressOn = false;
    private String mRecrodName;

    @BindView(R.id.photo_image)
    ImageView mImageView;
    @BindView(R.id.shutter_filler)
    ImageView mShutterFiller;
    @BindView(R.id.shutter)
    ImageView mShutter;


    public static PhotoImageFragment getInstance(String piideoName) {
        if (TextUtils.isEmpty(piideoName)) {
            throw new IllegalArgumentException("Piideo name is empty");
        }
        PhotoImageFragment f = new PhotoImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PIIDEO_NAME, piideoName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPiideoName = getArguments().getString(ARG_PIIDEO_NAME);
        requestPermissionAndLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_photo_image;
        View root = super.onCreateView(inflater, container, savedInstanceState);
        File pictures = new File(Recorder.HOME_PATH);
        File photoFile = new File(pictures, mPiideoName + ".jpg");
//        mImageView.setRotation(90);
        if (photoFile.exists()) {
            Bitmap myBitmap = decodeFile(photoFile);
            int rotationAngle = rotation(photoFile);
            myBitmap = rotateImage(myBitmap, rotationAngle);
            mImageView.setImageBitmap(myBitmap);
        }
        mShutter.setOnLongClickListener(v -> {
            Log.d(TAG, "onTouch: Long");
            startRecord(Recorder.getIntent(getActivity(), mPiideoName));
            mLongPressOn = true;
            return true;
        });
        mShutter.setOnTouchListener((view, event) -> {
            Log.d(TAG, "onTouch: ");
            if (mLongPressOn && event.getAction() == MotionEvent.ACTION_UP) {
                stopRecord(Recorder.getIntent(getActivity(), mPiideoName));
                Log.d(TAG, "onTouch: true");
                return true;
            }
            Log.d(TAG, "onTouch: false");
            return false;
        });
        return root;
    }


    private void startRecord(Intent intent) {
        mShutterFiller.setVisibility(View.VISIBLE);
        getActivity().startService(intent);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void stopRecord(Intent intent) {
        mShutterFiller.setVisibility(View.GONE);
        getActivity().stopService(intent);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent i = ChatActivity.getIntent(getActivity());
        getActivity().startActivity(i);
        getActivity().finish();
        savePiideoToDb();
    }

    private void savePiideoToDb() {
        ChatLab lab = ChatLab.get(getActivity().getApplicationContext());
        PiideoRow row = new PiideoRow();
        row.setPiideoFileName(mPiideoName);
        lab.addPiideo(row);
    }

    private Bitmap decodeFile(File photoFile) {
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(photoFile), null, o);
            //Find the correct scale value. It should be the power of 2.

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            int stageWidth = display.getWidth();
            int stageHeight = display.getHeight();

            final int REQUIRED_SIZE = stageHeight * stageWidth;
            int height_tmp = o.outHeight, width_tmp = o.outWidth;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale++;
            }

            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(photoFile), null, o2);
        } catch (FileNotFoundException e) {
            Log.e(TAG, photoFile.getAbsolutePath() + " doesn't exist");
            throw new RuntimeException(e);
        }
    }

    private void requestPermissionAndLoad() {
        if (ActivityCompat.checkSelfPermission(
                getActivity(), android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED /*||
                ActivityCompat.checkSelfPermission(
                        this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                        this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED*/)

        {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(
                        new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private int rotation(File photoFile) {
        try {
            ExifInterface ei = new ExifInterface(photoFile.getAbsolutePath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    return 0;

            }
        } catch (IOException ex) {
            Log.e(TAG, "Exif couldn't be loaded from file");
            throw new RuntimeException(ex);
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}
