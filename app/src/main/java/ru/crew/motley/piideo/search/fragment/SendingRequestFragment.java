package ru.crew.motley.piideo.search.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

import butterknife.BindView;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.search.SearchListener;
import ru.crew.motley.piideo.search.service.RequestService;

/**
 * Created by vas on 3/1/18.
 */

public class SendingRequestFragment extends ButterFragment {

    private static final String TAG = SendingRequestFragment.class.getSimpleName();

    private SearchListener mCallback;

    private Handler progressCounter = new Handler();
    private Timer progressUpdater;

    @BindView(R.id.progressBar)
    MaterialProgressBar mProgressBar;

    public static SendingRequestFragment newInstance(SearchListener callback) {
        Bundle args = new Bundle();
        SendingRequestFragment fragment = new SendingRequestFragment();
        fragment.setArguments(args);
        fragment.mCallback = callback;
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        RequestService.fragmentCallback(this);
//        if (!SharedPrefs.isSearching(getContext())) {
//            complete();
//        }

        int progressPosition = startProgress(SharedPrefs.loadStartSearchingTime(getContext()));
        mProgressBar.setMax(1000);
        mProgressBar.setProgress(progressPosition);
        progressUpdater = new Timer();
        progressCounter.postDelayed(progressUpdater, 200);
        if (SharedPrefs.searchCompleted(getContext())) {
            SharedPrefs.setSearching(false, getContext());
            SharedPrefs.setSearchCount(-1, getContext());
            complete();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        RequestService.fragmentCallback(null);
        progressCounter.removeCallbacks(progressUpdater);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_sending_request;
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    public void complete() {
        mCallback.onNext();
    }

//    private void onSearchComplete() {
//        mCallback.onNext();
//    }

    private int startProgress(long startTime) {
        long timeLeft = new Date().getTime() - startTime;
        return 1000 * (int) timeLeft / (SharedPrefs.getSearchCount(getContext()) * 90 * 1000);
    }


    private class Timer implements Runnable {

        @Override
        public void run() {
            if (getContext() == null) {
                return;
            }

                int value = startProgress(SharedPrefs.loadStartSearchingTime(getContext()));
                if (value < 1000) {
                    progressCounter.postDelayed(this, 200);
                }
                if (mProgressBar != null) {
                    mProgressBar.setProgress(value);
                }

        }

    }
}
