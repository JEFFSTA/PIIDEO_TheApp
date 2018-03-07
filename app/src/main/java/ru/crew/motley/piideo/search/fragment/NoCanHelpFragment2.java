package ru.crew.motley.piideo.search.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.search.SearchListener;

/**
 * Created by vas on 3/7/18.
 */

public class NoCanHelpFragment2 extends Fragment {

    private static final String TAG = NoCanHelpFragment2.class.getSimpleName();

    private SearchListener mCallback;

    public static NoCanHelpFragment2 newInstance() {
        Bundle args = new Bundle();
        NoCanHelpFragment2 fragment = new NoCanHelpFragment2();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_no_one_help_stub, container, false);
    }
}
