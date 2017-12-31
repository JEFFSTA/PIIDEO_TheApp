package ru.crew.motley.piideo.chat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.chat.db.PiideoRow;
import ru.crew.motley.piideo.piideo.PhotoActivity;

import static ru.crew.motley.piideo.piideo.service.Recorder.HOME_PATH;

/**
 * Created by vas on 12/22/17.
 */

public class ChatFragment extends ButterFragment implements ChatAdapter.PiideoCallback /*ChatAdapter.PlayerBarListener */{

    public interface PiideoShower {
        void showPiideo(String piideoFileName);
    }

    private static final String TAG = ChatFragment.class.getSimpleName();

    @BindView(R.id.chat_recycler)
    RecyclerView chatRecycler;

    private List<PiideoRow> mPiideoRows;
    private ChatAdapter mChatAdapter;
    private MediaPlayer mMediaPlayer;

    private PiideoShower mPiideoShower;

    private int mPosition = -1;

    private Handler mHandler;

    private Timer _timer = new Timer();

//    private Runnable mTimer = () -> {
//        if (mPosition != -1 && mMediaPlayer != null) {
//            ChatAdapter.ChatViewHolder holder = (ChatAdapter.ChatViewHolder)
//                    chatRecycler.findViewHolderForAdapterPosition(mPosition);
//            holder.syncBar(mMediaPlayer.getCurrentPosition() / 1000);
//        }
//        mHandler.postDelayed(mTimer, 1000);
//    }

    public static ChatFragment newInstance(PiideoShower piideoShower) {
        ChatFragment f = new ChatFragment();
        f.mPiideoShower = piideoShower;
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaPlayer = new MediaPlayer();
        mPiideoRows = ChatLab.get(getActivity().getApplicationContext()).getPiideos();
        mHandler = new Handler();
//        startRepeatingTask();
        if (mChatAdapter != null) {
            mChatAdapter.notifyDataSetChanged();
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (mChatAdapter != null) {
//            mChatAdapter.notifyDataSetChanged();
//        }
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_chat;
        if (getView() != null) {
            return getView();
        }
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mChatAdapter = new ChatAdapter(mPiideoRows, this);
        chatRecycler.setAdapter(mChatAdapter);
        chatRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        return v;
    }

    @OnClick(R.id.piideo)
    public void makePiideo() {
        Intent i = PhotoActivity.getIntent(getActivity());
        startActivity(i);
        getActivity().finish();
    }

/*    public void playStart(String resourceName) {
        try {
            releasePlayer();
            mMediaPlayer = new MediaPlayer();
//            if (mMediaPlayer != null) {
            String fullPath = filePath(resourceName);
            mMediaPlayer.setDataSource(fullPath);
            mMediaPlayer.setVolume(1f, 1f);
            mMediaPlayer.setOnCompletionListener(mp -> stop());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
//            }
        } catch (Exception e) {
            Log.e(TAG, "Error play", e);
        }
    }*/

    public void playStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    public void playPause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public void playResume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

/*    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }*/

/*    @Override
    public void play(String piideoFile, int position) {
        if (mPosition != position) {
            ChatAdapter.ChatViewHolder holder =
                    (ChatAdapter.ChatViewHolder) chatRecycler.findViewHolderForAdapterPosition(mPosition);
            if (holder != null) {
                holder.clearBar();
            }
            mPosition = position;
            playStart(piideoFile);
        } else {
            playResume();
        }
        startRepeatingTask();
//        _timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                // use runOnUiThread(Runnable action)
//                getActivity().runOnUiThread(() -> {
//                    if (mPosition != -1 && mMediaPlayer != null) {
//                        ChatAdapter.ChatViewHolder holder = (ChatAdapter.ChatViewHolder)
//                                chatRecycler.findViewHolderForAdapterPosition(mPosition);
//                        holder.syncBar(mMediaPlayer.getCurrentPosition() / 1000);
//                    }
//                });
//            }
//        }, 1000);
    }*/

    /*@Override
    public void pause(int position) {
        mPosition = position;
        playPause();
        stopRepeatingTask();
        ChatAdapter.ChatViewHolder holder = (ChatAdapter.ChatViewHolder)
                chatRecycler.findViewHolderForAdapterPosition(mPosition);
        holder.syncBar(mMediaPlayer.getCurrentPosition() / 1000, mMediaPlayer.getDuration() / 1000);
    }*/

/*    @Override
    public void stop() {
        ChatAdapter.ChatViewHolder holder =
                (ChatAdapter.ChatViewHolder) chatRecycler.findViewHolderForAdapterPosition(mPosition);
        if (holder != null) {
            holder.clearBar();
        }
        mPosition = -1;
        playStop();
        stopRepeatingTask();
    }*/

    private String filePath(String name) throws IOException {
        String recName = name + ".mp4";
        return HOME_PATH + recName;
    }

/*    Runnable mSeekBarUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                if (mPosition != -1 && mMediaPlayer != null) {
                    ChatAdapter.ChatViewHolder holder = (ChatAdapter.ChatViewHolder)
                            chatRecycler.findViewHolderForAdapterPosition(mPosition);
                    holder.syncBar(mMediaPlayer.getCurrentPosition() / 1000, mMediaPlayer.getDuration() / 1000);
                }
            } finally {
                mHandler.postDelayed(mSeekBarUpdater, 1000);
            }
        }
    };*/

/*    void startRepeatingTask() {
        mSeekBarUpdater.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mSeekBarUpdater);
    }*/

    @Override
    public void onClick(String piideoFileName) {
        mPiideoShower.showPiideo(piideoFileName);
    }
}
