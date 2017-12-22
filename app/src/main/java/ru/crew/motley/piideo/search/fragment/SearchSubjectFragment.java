package ru.crew.motley.piideo.search.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.search.SearchListener;

/**
 * Created by vas on 12/18/17.
 */

public class SearchSubjectFragment extends ButterFragment {

    private SearchListener mSearchListener;

    @BindView(R.id.subject)
    EditText subject;

    public static SearchSubjectFragment newInstance(SearchListener listener) {
        Bundle args = new Bundle();
        SearchSubjectFragment fragment = new SearchSubjectFragment();
        fragment.setArguments(args);
        fragment.mSearchListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_search_subject;
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @OnClick(R.id.next_btn)
    public void showSearch() {
        if (subject.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), R.string.sch_subject_violation, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        SharedPrefs.searchSubject(subject.getText().toString(), getActivity());
        mSearchListener.onNext();
    }


}
