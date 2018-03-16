package ru.crew.motley.piideo.piideo.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.BindView;
import dagger.android.support.AndroidSupportInjection;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.activity.ChatActivity;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.chat.db.PiideoRow;
import ru.crew.motley.piideo.fcm.FcmMessage;
import ru.crew.motley.piideo.fcm.MessagingService;
import ru.crew.motley.piideo.piideo.BitmapSingleton;
import ru.crew.motley.piideo.piideo.service.Recorder;
import ru.crew.motley.piideo.util.Utils;




/**
 * Created by vas on 12/22/17.
 */

public class PhotoImageFragment extends ButterFragment {

    private static final String TAG = PhotoImageFragment.class.getSimpleName();

    private static final String ARG_PIIDEO_NAME = "piideoName";
    private static final String ARG_FILTER_PATH = "filter";
    private static final String ARG_MESSAGE = "fcm_message";
    private static final String ARG_MESSAGE_ID = "local_db_id";

    private FcmMessage mFcmMessage;
    private String mPiideoName;
    private boolean mLongPressOn = false;
    private String mRecrodName;
    private String mMessageId;

    @BindView(R.id.photo_image)
    ImageView mImageView;
    @BindView(R.id.shutter_filler)
    ImageView mShutterFiller;
    @BindView(R.id.shutter)
    ImageView mShutter;


    public static PhotoImageFragment getInstance(String piideoName, Parcelable message, String dbMessageId) {
        if (TextUtils.isEmpty(piideoName)) {
            throw new IllegalArgumentException("Piideo name is empty");
        }
        PhotoImageFragment f = new PhotoImageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MESSAGE, message);
        args.putString(ARG_PIIDEO_NAME, piideoName);
        args.putString(ARG_MESSAGE_ID, dbMessageId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPiideoName = getArguments().getString(ARG_PIIDEO_NAME);
        mFcmMessage = Parcels.unwrap(getArguments().getParcelable(ARG_MESSAGE));
        mMessageId = getArguments().getString(ARG_MESSAGE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_photo_image;
        View root = super.onCreateView(inflater, container, savedInstanceState);
        File pictures = new File(Recorder.HOME_PATH);
        File photoFile = new File(pictures, mPiideoName + ".jpg");

        Bitmap cached = BitmapSingleton.image();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int stageWidth = display.getWidth();
        int stageHeight = display.getHeight();

        final int REQUIRED_SIZE = stageHeight * stageWidth;
        int height_tmp = cached.getHeight(), width_tmp = cached.getWidth();
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale++;
        }

        mImageView.setImageBitmap(Bitmap.createScaledBitmap(cached, cached.getWidth() / scale, cached.getHeight() / scale, false));
        cached = null;


//        if (photoFile.exists()) {
//            Bitmap myBitmap = decodeFile(photoFile);
//
//            mImageView.setImageBitmap(myBitmap);
//        }
//        mShutter.setOnLongClickListener(v -> {
//            Log.d(TAG, "onTouch: Long");
//            startRecord(Recorder.getIntent(getActivity(), mPiideoName));
//            mLongPressOn = true;
//            return true;
//        });
//        mShutter.setOnTouchListener((view, event) -> {
//            Log.d(TAG, "onTouch: ");
//            if (mLongPressOn && event.getAction() == MotionEvent.ACTION_UP) {
//                stopRecord(Recorder.getIntent(getActivity(), mPiideoName));
//                Log.d(TAG, "onTouch: true");
//                return true;
//            }
//            Log.d(TAG, "onTouch: false");
//            return false;
//        });
        prepareShutterButton();
        return root;
    }

    private void prepareShutterButton() {
        mShutter.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                mShutter.setEnabled(false);
                // start recording.
                Log.d("Shutter", " DOWN");
                startRecord(Recorder.getIntent(getActivity(), mPiideoName));
                int colorId = ContextCompat.getColor(getContext(), android.R.color.holo_green_light);
                ((TextView) getView().findViewById(R.id.button_text)).setTextColor(colorId);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
//                mShutter.setEnabled(true);
                // Stop recording and save file
                Log.d("Shutter", " UP");
                stopRecord(Recorder.getIntent(getActivity(), mPiideoName));
                return true;
            }
            return false;
        });
    }

    private void sendPiideoMessage() {
        long timestamp = Utils.Companion.gmtTimeInMillis();
        FcmMessage message = new FcmMessage(
                timestamp,
                -timestamp,
                Utils.Companion.gmtDayTimestamp(timestamp),
                mFcmMessage.getTo(),
                mFcmMessage.getFrom(),
                mPiideoName,
                MessagingService.PDO,
                mFcmMessage.getTo() + "_" + mFcmMessage.getFrom(),
                false);

        FirebaseDatabase.getInstance()
                .getReference()
                .child("messages")
                .child(mFcmMessage.getTo())
                .child(mFcmMessage.getFrom())
                .push()
                .setValue(message);
    }

    private void startRecord(Intent intent) {
//        mShutterFiller.setVisibility(View.VISIBLE);
        getActivity().startService(intent);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        enlargeRecButton();
    }

    private void enlargeRecButton() {
        int newSquareSize = (int) (mShutter.getWidth() * 1.2);
        ResizeAnimation resizeAnimation = new ResizeAnimation(
                mShutter,
                (int) (mShutter.getWidth() * 1.25),
                mShutter.getWidth()
        );
        resizeAnimation.setDuration(200);
        mShutter.startAnimation(resizeAnimation);
//        ViewGroup sceneRoot = (ViewGroup) mShutter.getParent();
//        int newSquareSize = (int) (mShutter.getWidth() * 1.2);
//        ChangeBounds transition = new ChangeBounds();
//        transition.setDuration(200);
//        TransitionManager.beginDelayedTransition(sceneRoot, transition);
//        ViewGroup.LayoutParams params = mShutter.getLayoutParams();
//
//        params.width = newSquareSize;
//        params.height = newSquareSize;
//        mShutter.setLayoutParams(params);
    }

    private void reduceRecButton() {
        ResizeAnimation resizeAnimation = new ResizeAnimation(
                mShutter,
                (int) (mShutter.getWidth() * 0.8),
                mShutter.getWidth()
        );
        resizeAnimation.setDuration(100);
        mShutter.startAnimation(resizeAnimation);
//        ViewGroup sceneRoot = (ViewGroup) mShutter.getParent();
//        int newSquareSize = (int) (mShutter.getWidth() * 0.84);
//        ChangeBounds transition = new ChangeBounds();
//        transition.setDuration(20);
//        TransitionManager.beginDelayedTransition(sceneRoot, transition);
//        ViewGroup.LayoutParams params = mShutter.getLayoutParams();
//        params.width = newSquareSize;
//        params.height = newSquareSize;
//        mShutter.setLayoutParams(params);
    }

    private void stopRecord(Intent intent) {
//        mShutterFiller.setVisibility(View.GONE);
        getActivity().stopService(intent);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        reduceRecButton();
        // TODO: 1/20/18 remove null from getIntent
        Intent i = ChatActivity.getIntent(mMessageId, getActivity());
        getActivity().startActivity(i);
        getActivity().finish();
        savePiideoToDb();
        sendPiideoMessage();
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

    @Override
    public void onDestroy() {
        BitmapSingleton.clear();
        super.onDestroy();
    }
}
