package ru.crew.motley.piideo.search.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.network.neo.transaction.Data;

/**
 * Created by vas on 12/18/17.
 */

public class SearchResultFragment extends ButterFragment {

    @BindView(R.id.debug_result)
    TextView mDebugText;

    public static SearchResultFragment newInstance() {
        Bundle args = new Bundle();
        SearchResultFragment fragment = new SearchResultFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startSearch();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_search_result;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void startSearch() {
        String searchSubject = SharedPrefs.getSearchSubject(getActivity());
        String phoneNumber = SharedPrefs.getMemberPhone(getActivity());
        Statement search = searchRequest(searchSubject, phoneNumber);
        Statements statements = new Statements();
        statements.getValues().add(search);
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            List<Data> responseData = transaction.getResults().get(0).getData();
                            if (responseData.isEmpty()) {
                                Toast.makeText(getActivity(), R.string.sch_no_result, Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }
                            String response = responseData
                                    .get(0)
                                    .getRow()
                                    .get(0)
                                    .getValue();
                            mDebugText.setText(response);
                        },
                        error -> {

                        });
    }

    private Statement searchRequest(String searchSubject, String phoneNumber) {
        if (TextUtils.isEmpty(searchSubject)) {
            throw new IllegalStateException("Search subject can't be null or empty");
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            throw new IllegalArgumentException("Phone number can't be null or empty");
        }
        Statement subject = new Statement();
        subject.setStatement(Request.FIND_QUESTION_TARGET);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, phoneNumber);
        parameters.getProps().put(Request.Var.NAME, searchSubject);
        subject.setParameters(parameters);
        return subject;
    }


}
