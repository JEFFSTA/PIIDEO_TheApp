package ru.crew.motley.piideo.search.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.search.SearchListener;

/**
 * Created by vas on 3/1/18.
 */

public class NoHelpFragment extends ButterFragment {

    private static final String TAG = NoHelpFragment.class.getSimpleName();

    private SearchListener mCallback;

    @BindView(R.id.subject)
    TextView subject;
    @BindView(R.id.topic)
    TextView topic;

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
        View v = super.onCreateView(inflater, container, savedInstanceState);
        subject.setText(SharedPrefs.getSearchSubject(getContext()));
        topic.setText(SharedPrefs.getRequestMessage(getContext()));
        return v;
    }


}
