package ru.crew.motley.piideo.search.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
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
import ru.crew.motley.piideo.network.neo.transaction.Result;
import ru.crew.motley.piideo.network.neo.transaction.Row;
import ru.crew.motley.piideo.search.SearchListener;
import ru.crew.motley.piideo.search.adapter.SubjectAdapter;

import static android.content.ContentValues.TAG;

/**
 * Created by vas on 12/18/17.
 */

public class SearchSubjectFragment extends ButterFragment implements SubjectAdapter.SubjectListener {

    private SearchListener mSearchListener;

//    @BindView(R.id.subject)
//    EditText mSubject;
    @BindView(R.id.subjectRecycler)
    RecyclerView mRecyclerView;

    private List<String> mPhones = new ArrayList<>();
    private List<String> mSubjects = new ArrayList<>();

    private SubjectAdapter mSubjectAdapter;

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
        loadContactsPhones();
        syncContacts();
        mSubjectAdapter = new SubjectAdapter(mSubjects, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_search_subject;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mSubjectAdapter);
        loadAllSubjects();
        return v;
    }


//    @OnClick(R.id.next_btn)
    public void showSearch() {
//        if (mSubject.getText().toString().isEmpty()) {
//            Toast.makeText(getActivity(), R.string.sch_subject_violation, Toast.LENGTH_SHORT)
//                    .show();
//            return;
//        }
//        SharedPrefs.searchSubject(mSubject.getText().toString(), getActivity());
        mSearchListener.onNext();
    }

    private void loadContactsPhones() {
        Cursor managedCursor = getActivity().getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{
                                ContactsContract.CommonDataKinds.Phone._ID,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER},
                        null,
                        null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        try {
            if (managedCursor != null && managedCursor.getCount() > 0) {
                managedCursor.moveToFirst();
                while (!managedCursor.isAfterLast()) {
                    String phone = managedCursor.getString(managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phone = phone.replaceAll("\\D", "");
                    if (phone.startsWith("33")) {
                        phone = phone.replaceFirst("33", "");
                    } else if (phone.startsWith("7")) {
                        phone = phone.replaceFirst("7", "");
                    } else if (phone.startsWith("8")) {
                        phone = phone.replaceFirst("8", "");
                    }
//                    if (phone.startsWith("0")) {
//                        phone = phone.replaceFirst("0", "");
//                    }
                    mPhones.add(phone);
                    managedCursor.moveToNext();
                }
            }
        } finally {
            managedCursor.close();
        }
    }

    private void syncContacts() {
        String phoneNumber = SharedPrefs.getMemberPhone(getActivity());
        Statement search = searchRequest(phoneNumber);
        Statements statements = new Statements();
        statements.getValues().add(search);
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
//                            List<Data> responseData = transaction.getResults().get(0).getData();
//                            if (responseData.isEmpty()) {
//                                Toast.makeText(getActivity(), R.string.sch_no_result, Toast.LENGTH_SHORT)
//                                        .showChat();
                            return;
//                            }
                        },
                        error -> {

                        });
    }

    private Statement searchRequest(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            throw new IllegalArgumentException("Phone number can't be null or empty");
        }
        Statement subject = new Statement();
        String nums = TextUtils.join("', '", mPhones);
        nums = "'" + nums + "'";
        String st = Request.DELETE_CONTACT + "[" + nums + "] delete rs";
        subject.setStatement(st);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, phoneNumber);
//        parameters.getProps().put("nums", TextUtils.join(",", mPhones));
        subject.setParameters(parameters);
        return subject;
    }

    private void loadAllSubjects() {
        Statement search = subjectRequest();
        Statements statements = new Statements();
        statements.getValues().add(search);
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            mSubjects.clear();
                            Log.d(TAG, "" + transaction.toString());
                            for (Result result : transaction.getResults()) {
                                List<Data> data = result.getData();
                                for (Data datum: data) {
                                    List<Row> rows = datum.getRow();
                                    for(Row row : rows) {
                                        String json = row.getValue();
                                        JSONObject response = new JSONObject(json);
                                        String subject = response.getString("name");
                                        mSubjects.add(subject);
                                    }
                                }
                            }
                            mSubjectAdapter.notifyDataSetChanged();

                        },
                        error -> {

                        });
    }

    private Statement subjectRequest() {
        Statement subject = new Statement();
        String st = Request.FIND_ALL_SUBJECTS;
        subject.setStatement(st);
        Parameters parameters = new Parameters();
        subject.setParameters(parameters);
        return subject;
    }

    @Override
    public void onClick(String subject) {
        SharedPrefs.searchSubject(subject, getActivity());
        mSearchListener.onNext();
    }
}
