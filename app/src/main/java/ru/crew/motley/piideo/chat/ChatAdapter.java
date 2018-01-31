package ru.crew.motley.piideo.chat;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;


import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.db.PiideoRow;

import static ru.crew.motley.piideo.piideo.service.Recorder.HOME_PATH;

/**
 * Created by vas on 12/26/17.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<PiideoRow> mPiideoRows;
    private PiideoCallback mPiideoCallback;
//    private MediaPlayer mMediaPlayer;
//    private PlayerBarListener mPlayerBarListener;

//    public interface PlayerBarListener {
//        void play(String piideoFile, int position);
//        void togglePause(int position);
//        void stop();
//    }

    public interface PiideoCallback {
        void onClick(String piideoFileName);
    }

    public ChatAdapter(List<PiideoRow> piideoRows/*, PlayerBarListener listener*/, PiideoCallback callback) {
        mPiideoRows = piideoRows;
        mPiideoCallback = callback;
//        mMediaPlayer = mediaPlayer;
//        mPlayerBarListener = listener;
    }


    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        PiideoRow row = mPiideoRows.get(position);
        try {
            holder.bind(row);
        } catch (IOException ex) {
            Log.e("ChatAdapter", " Error while loading image file " + row.getPiideoFileName());
        }
    }

    @Override
    public int getItemCount() {
        return mPiideoRows.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        private PiideoRow mPiideoRow;

        private ImageView mPlayerToggler;
        private SeekBar mSeekBar;

        private boolean isPlaying = false;
        private boolean isActive;

        ChatViewHolder(View itemView) {
            super(itemView);
            mPlayerToggler = itemView.findViewById(R.id.player_toggler);
//            mPlayerToggler.setOnClickListener(v -> {
//                isPlaying = !isPlaying;
//                if (isPlaying) {
//                    mPlayerBarListener.play(mPiideoRow.getPiideoFileName(), getAdapterPosition());
//                    mPlayerToggler.setImageResource(R.drawable.ic_pause_24dp);
//                } else {
//                    mPlayerBarListener.togglePause(getAdapterPosition());
//                    mPlayerToggler.setImageResource(R.drawable.ic_play_24dp);
//                }
//            });
//            mSeekBar = itemView.findViewById(R.id.);
        }

        void bind(PiideoRow row) throws IOException {
            mPiideoRow = row;
            String filePath = filePath(mPiideoRow.getPiideoFileName());
            itemView.setOnClickListener(v -> {
                mPiideoCallback.onClick(mPiideoRow.getPiideoFileName());
            });
            Picasso.with(mPlayerToggler.getContext())
                    .load(new File(filePath))
//                    .rotate(90)
//                    .resize(800, 80)
                    .fit()
                    .centerCrop()
                    .into(mPlayerToggler);
        }

//        public void syncBar(int offset, int duration) {
//            mSeekBar.setProgress(offset);
//            mSeekBar.setMax(duration);
//        }

//        public void clearBar() {
//            mSeekBar.setProgress(0);
//            mPlayerToggler.setImageResource(R.drawable.ic_play_24dp);
//            isPlaying = false;
//        }

        private String filePath(String name) throws IOException {
            String recName = name + ".jpg";
            return HOME_PATH + recName;
        }
    }
}
