package ru.crew.motley.piideo.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.OnClick;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.piideo.PhotoActivity;

/**
 * Created by vas on 12/22/17.
 */

public class ChatFragment extends ButterFragment {

    public static ChatFragment newInstance() {
        ChatFragment f = new ChatFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_chat;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnClick(R.id.piideo)
    public void makePiideo() {
        Intent i = PhotoActivity.getIntent(getActivity());
        startActivity(i);
    }
}
