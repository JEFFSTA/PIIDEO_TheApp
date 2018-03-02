package ru.crew.motley.piideo.search.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.search.SearchListener;
import ru.crew.motley.piideo.search.SearchRepeaterSingleton;

/**
 * Created by vas on 3/1/18.
 */

public class SendingRequestFragment extends ButterFragment {

    private static final String TAG = SendingRequestFragment.class.getSimpleName();

    private SearchListener mCallback;
    private SearchRepeaterSingleton mSearchRepeaterSingleton;
    private CompositeDisposable mDisposables = new CompositeDisposable();

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
        mDisposables = new CompositeDisposable();
        if (mSearchRepeaterSingleton == null) {
            mSearchRepeaterSingleton = SearchRepeaterSingleton.instance(getActivity());
        }
        int progressPosition = startProgress(SharedPrefs.loadProgressTime(getContext()));
        mProgressBar.setMax(1000);
        mProgressBar.setProgress(progressPosition);
        progressUpdater = new Timer();
        if (!mSearchRepeaterSingleton.isOn()) {
            mSearchRepeaterSingleton.start();
        }
        progressCounter.postDelayed(progressUpdater, 200);
        subscribeOnSearch();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSearchRepeaterSingleton = null;
        mDisposables.dispose();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_sending_request;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void subscribeOnSearch() {
        Disposable searchSubscription = mSearchRepeaterSingleton.subject()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next -> {
                            Log.d(TAG, "First one passed");
                        },
                        error -> {
                            Log.e(TAG, "Error subscription onCLick");
                            Log.e(TAG, "", error);
                        },
                        () -> {
                            Log.d(TAG, "OnComplete");
                            onSearchComplete();
                        });
        mDisposables.add(searchSubscription);
    }

    private void onSearchComplete() {
        mCallback.onNext();
    }

    private int startProgress(long startTime) {
        long timeLeft = new Date().getTime() - startTime;
        return 1000 * (int) timeLeft / (mSearchRepeaterSingleton.count() * 50 * 1000);
    }

    private int secondsToEnd(long startTIme) {
        return (int) (startTIme - new Date().getTime()) / 60;
    }


    private class Timer implements Runnable {

        @Override
        public void run() {
            if (getContext() == null) {
                return;
            }
            int value = startProgress(SharedPrefs.loadProgressTime(getContext()));
            if (value < 1000) {
                progressCounter.postDelayed(this, 200);
            }
            if (mProgressBar != null) {
                mProgressBar.setProgress(value);
            }
        }

        private String timeMin(long seconds) {
            long min = seconds / 60;
            if (min > 9) {
                return "" + min;
            }
            return "0" + min;
        }

        private String timeSec(long seconds) {
            long sec = seconds % 60;
            if (sec > 9) {
                return "" + sec;
            }
            return "0" + sec;
        }
    }
}
