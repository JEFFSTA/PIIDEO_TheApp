package ru.crew.motley.piideo.handshake.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.handshake.NavigationCallback;

/**
 * Created by vas on 2/24/18.
 */

public class HandshakeTimeoutFragment extends Fragment {

    private static final int NEXT_SCREEN_TIMEOUT = 2_000;

    private NavigationCallback mCallback;
    private Runnable mRedirectCall = () -> mCallback.end();
    private Handler mHandler = new Handler();

    public static HandshakeTimeoutFragment newInstance(NavigationCallback callback) {
        Bundle args = new Bundle();
        HandshakeTimeoutFragment fragment = new HandshakeTimeoutFragment();
        fragment.setArguments(args);
        fragment.mCallback = callback;
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(mRedirectCall, NEXT_SCREEN_TIMEOUT);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRedirectCall);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_handshake_timeout, container, false);
        return v;
    }

}
