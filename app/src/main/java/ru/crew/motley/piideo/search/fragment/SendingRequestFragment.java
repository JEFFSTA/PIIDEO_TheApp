package ru.crew.motley.piideo.search.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
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
        if (!mSearchRepeaterSingleton.isOn()) {
            mSearchRepeaterSingleton.start();
        }
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
}
