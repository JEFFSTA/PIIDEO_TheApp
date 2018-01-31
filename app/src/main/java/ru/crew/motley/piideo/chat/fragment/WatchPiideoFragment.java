package ru.crew.motley.piideo.chat.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;

import static ru.crew.motley.piideo.piideo.service.Recorder.HOME_PATH;

public class WatchPiideoFragment extends ButterFragment {

    private static final String TAG = WatchPiideoFragment.class.getSimpleName();
    private static final String ARG_PIIDEO_FILE_NAME = "piideoFileName";

    @BindView(R.id.piideoImage)
    ImageView mPiideoImage;
    @BindView(R.id.piideoSeekbar)
    SeekBar mPiideoSeekbar;

    private String mPiideoFileName;
    private MediaPlayer mMediaPlayer;
    private ChatShower mChatShower;

    private long mDuration;
    private boolean playing;

    public interface ChatShower {
        void showChat();
    }

    public static WatchPiideoFragment newInstance(ChatShower shower, String piideoFileName) {
        if (TextUtils.isEmpty(piideoFileName)) {
            throw new IllegalArgumentException("Piideo file name can't be empty or null.");
        }
        Bundle args = new Bundle();
        args.putString(ARG_PIIDEO_FILE_NAME, piideoFileName);
        WatchPiideoFragment fragment = new WatchPiideoFragment();
        fragment.mChatShower = shower;
        fragment.setArguments(args);
        return fragment;
    }

    private Bitmap mBitmap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPiideoFileName = getArguments().getString(ARG_PIIDEO_FILE_NAME);
        mDuration = getDuration();
        try {
            String fullName = fileImagePath(mPiideoFileName);
            mBitmap = decodeFile(new File(fullName));
//            Picasso.with(getActivity()).load(fullName).fetch();
        } catch (IOException ex) {
            Log.e(WatchPiideoFragment.class.getSimpleName(), "ERRR", ex);
        }
    }

    public void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void playStart() {
        try {
            releasePlayer();
            mMediaPlayer = new MediaPlayer();
            String fullPath = fileAudioPath(mPiideoFileName);
            mMediaPlayer.setDataSource(fullPath);
            mMediaPlayer.setVolume(1f, 1f);
            mMediaPlayer.setOnCompletionListener(l -> mChatShower.showChat());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            playing = true;
            mPiideoSeekbar.setMax(mMediaPlayer.getDuration());
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (mMediaPlayer != null && mPiideoSeekbar != null) {
                        mPiideoSeekbar.setProgress(mMediaPlayer.getCurrentPosition());
                    }
                }
            }, 0, 50);
        } catch (Exception e) {
            Log.e(TAG, "Error play", e);
        }
    }

    public void togglePause() {
        if (mMediaPlayer != null) {
            playing = !playing;
            if (playing) {
                mMediaPlayer.start();
            } else {
                mMediaPlayer.pause();
            }
        }
    }

    private void prepareOnImagePause() {
        mPiideoImage.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // start recording.
                Log.d("Shutter", " DOWN");
                togglePause();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Stop recording and save file
                Log.d("Shutter", " UP");
                togglePause();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private long getDuration() {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(fileAudioPath(mPiideoFileName));
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Long.parseLong(durationStr) / 100;
        } catch (IOException ex) {
            Log.e(TAG, "Piideo duration exception", ex);
            return -1;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_watch_piideo;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mPiideoImage.setImageBitmap(mBitmap);
        prepareOnImagePause();
        new Handler().postDelayed(() -> playStart(), 500);
//        try {
//            String fullName = fileImagePath(mPiideoFileName);
//            Picasso.with(getActivity())
//                    .load(new File(fullName))
//                    .into(mPiideoImage, new Callback() {
//                        @Override
//                        public void onSuccess() {
//                            new Handler().postDelayed(() -> playStart(), 500);
//                        }
//
//                        @Override
//                        public void onError() {
//                            Log.e(TAG, "Error picasso watch piideo callback");
//                        }
//                    });
//        } catch (IOException ex) {
//            Log.e(TAG, "Error while loading piideo image " + mPiideoFileName);
//        }
        return v;
    }

    private String fileImagePath(String name) throws IOException {
        String recName = name + ".jpg";
        return HOME_PATH + recName;
    }

    private String fileAudioPath(String name) throws IOException {
        String recName = name + ".mp4";
        return HOME_PATH + recName;
    }


    private Bitmap decodeFile(File photoFile) {
        try {
            //decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(photoFile), null, options);
//            int imageHeight = options.outHeight;
//            int imageWidth = options.outWidth;
//            String imageType = options.outMimeType;
            //Find the correct scale value. It should be the power of 2.

            Point displaySize = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
            int stageWidth = displaySize.x;
            int stageHeight = displaySize.y;

            int inSampleSize = calculateInSampleSize(options, stageWidth, 0);
//            final int REQUIRED_SIZE = stageHeight * stageWidth;
//            int height_tmp = options.outHeight, width_tmp = options.outWidth;
//            int scale = 1;
//            while (true) {
//                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
//                    break;
//                width_tmp /= 2;
//                height_tmp /= 2;
//                scale++;
//            }

            //decode with inSampleSize
//            BitmapFactory.Options o2 = new BitmapFactory.Options();
//            o2.inSampleSize = scale;

            options.inSampleSize = inSampleSize;

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);


//            return BitmapFactory.decodeStream(new FileInputStream(photoFile), null, o2);
        } catch (FileNotFoundException e) {
            Log.e(TAG, photoFile.getAbsolutePath() + " doesn't exist");
            throw new RuntimeException(e);
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
//        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (width > reqWidth) {

//            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
