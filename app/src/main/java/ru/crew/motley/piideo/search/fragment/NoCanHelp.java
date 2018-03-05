package ru.crew.motley.piideo.search.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.OnClick;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;

/**
 * Created by vas on 3/3/18.
 */

public class NoCanHelp extends ButterFragment {

    private NoOneCanHelpCallback mCallback;

    public interface NoOneCanHelpCallback {
        void showStartSearch();
    }

    public static NoCanHelp newInstance(NoOneCanHelpCallback callback) {
        Bundle args = new Bundle();
        NoCanHelp fragment = new NoCanHelp();
        fragment.setArguments(args);
        fragment.mCallback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_no_one_help;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnClick(R.id.redirect)
    public void showSearchStart() {
        mCallback.showStartSearch();
    }
}
