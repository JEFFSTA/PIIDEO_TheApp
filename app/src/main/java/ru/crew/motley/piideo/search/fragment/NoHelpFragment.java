package ru.crew.motley.piideo.search.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.search.SearchListener;

/**
 * Created by vas on 3/1/18.
 */

public class NoHelpFragment extends ButterFragment {

    private static final String TAG = NoHelpFragment.class.getSimpleName();

    private SearchListener mCallback;

    public static NoHelpFragment newInstance(SearchListener callback) {
        Bundle args = new Bundle();
        NoHelpFragment fragment = new NoHelpFragment();
        fragment.setArguments(args);
        fragment.mCallback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_no_help;
        return super.onCreateView(inflater, container, savedInstanceState);
    }


}
