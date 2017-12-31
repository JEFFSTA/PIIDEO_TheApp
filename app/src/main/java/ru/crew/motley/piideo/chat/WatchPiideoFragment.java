package ru.crew.motley.piideo.chat;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;

import static ru.crew.motley.piideo.piideo.service.Recorder.HOME_PATH;

public class WatchPiideoFragment extends ButterFragment {

    private static final String TAG = WatchPiideoFragment.class.getSimpleName();
    private static final String ARG_PIIDEO_FILE_NAME = "piideoFileName";

    @BindView(R.id.piideoImage)
    ImageView mPiideoImage;

    private String mPiideoFileName;
    private MediaPlayer mMediaPlayer;
    private ChatShower mChatShower;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPiideoFileName = getArguments().getString(ARG_PIIDEO_FILE_NAME);
        try {
            String fullName = fileImagePath(mPiideoFileName);
            Picasso.with(getActivity()).load(fullName).fetch();
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
        } catch (Exception e) {
            Log.e(TAG, "Error play", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_watch_piideo;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        try {
            String fullName = fileImagePath(mPiideoFileName);
            Picasso.with(getActivity())
                    .load(new File(fullName))
                    .into(mPiideoImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            new Handler().postDelayed(() -> playStart(), 500);
                        }

                        @Override
                        public void onError() {

                        }
                    });
        } catch (IOException ex) {
            Log.e(TAG, "Error while loading piideo image " + mPiideoFileName);
        }
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
}
