package ru.crew.motley.piideo.search.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.NetworkErrorCallback;
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
import static ru.crew.motley.piideo.registration.fragments.PhoneFragment.MOROCCO_LENGTH;

public class SearchSubjectFragment extends ButterFragment implements SubjectAdapter.SubjectListener {

    private static final String ARG_MEMBER = "member";

    private static final int REQUEST_DELAY = 5;

    private SearchListener mSearchListener;
    private NetworkErrorCallback mErrorCallback;

    @BindView(R.id.subjectRecycler)
    RecyclerView mRecyclerView;
    private Member mMember;
    private List<String> mPhones = new ArrayList<>();
    private List<String> mSubjects = new ArrayList<>();
    private Queue<Member> mMembers = new LinkedList<>();

//    private Disposable mSearchRepeater;

//    private DatabaseReference mDatabase;

    private SubjectAdapter mSubjectAdapter;

//    private SearchRepeaterSingleton mSearchRepeaterSingleton;

    public static SearchSubjectFragment newInstance(Parcelable member, SearchListener listener, NetworkErrorCallback errorCallback) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEMBER, member);
        SearchSubjectFragment fragment = new SearchSubjectFragment();
        fragment.setArguments(args);
        fragment.mSearchListener = listener;
        fragment.mErrorCallback = errorCallback;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMember = Parcels.unwrap(getArguments().getParcelable(ARG_MEMBER));
        loadContactsPhones();
        syncContacts();
//        mDatabase = FirebaseDatabase.getInstance().getReference();
        mSubjectAdapter = new SubjectAdapter(mSubjects, this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_search_subject;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setAdapter(mSubjectAdapter);
        loadUsedSubjects();
        return v;
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
        List<String> phones = new ArrayList<>();
        try {
            if (managedCursor != null && managedCursor.getCount() > 0) {

                managedCursor.moveToFirst();
                while (!managedCursor.isAfterLast()) {
                    String phone = managedCursor.getString(managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phones.add(phone);
                    managedCursor.moveToNext();
                }
            }

        } finally {
            managedCursor.close();
        }
        for (String phone : phones) {
            phone = phone.replaceAll("\\D", "");
            if (phone.startsWith("33")) {
                phone = phone.replaceFirst("33", "");
            } else if (phone.length() > MOROCCO_LENGTH &&
                    (phone.startsWith("212") || phone.startsWith("213") || phone.startsWith("234"))) {
                phone = phone.substring(3);
            } else if (phone.startsWith("7")) {
                phone = phone.replaceFirst("7", "");
            } else if (phone.startsWith("8")) {
                phone = phone.replaceFirst("8", "");
            }
            if (phone.startsWith("0")) {
                phone = phone.replaceFirst("0", "");
            }
            mPhones.add(phone);
        }
//        if (!phones.isEmpty()) {
//            mPhones.clear();
//            mPhones.addAll(phones);
//        }
    }

    private void syncContacts() {
        String phoneNumber = mMember.getPhoneNumber();
        Statement search = deleteContactsRequest(phoneNumber);
        Statements statements = new Statements();
        statements.getValues().add(search);
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next -> {
                        },
                        error -> {
                            Log.e(TAG, "Contacts deletion request execution problem", error);
//                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
//                                    .show();
//                            if (!(error instanceof SocketTimeoutException)) {
//                                throw new RuntimeException(error);
//                            }
                            mErrorCallback.onError();
                        });
    }

    private Statement deleteContactsRequest(String phoneNumber) {
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
        subject.setParameters(parameters);
        return subject;
    }

    private void loadUsedSubjects() {
        Statement search = subjectRequest();
        Statements statements = new Statements();
        statements.getValues().add(search);
        NeoApi api = NeoApiSingleton.getInstance();
        final long subjectsLoadingStart = System.currentTimeMillis();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            long subjectsLoadingTime = System.currentTimeMillis() - subjectsLoadingStart;
                            Answers.getInstance()
                                    .logCustom(
                                            new CustomEvent("Loading search subjects")
                                                    .putCustomAttribute("Subjects loading time", subjectsLoadingTime));
                            mSubjectAdapter.notifyDataSetChanged();
                            mSubjects.clear();
                            Log.d(TAG, "" + transaction.toString());
                            for (Result result : transaction.getResults()) {
                                List<Data> data = result.getData();
                                for (Data datum : data) {
                                    List<Row> rows = datum.getRow();
                                    for (Row row : rows) {
                                        String json = row.getValue();
                                        JSONObject response = new JSONObject(json);
                                        String subject = response.getString("name");
                                        mSubjects.add(subject);
                                    }
                                }
                            }
                        },
                        error -> {
                            Log.e(TAG, "Used subjects loading problem", error);
//                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
//                                    .show();
//                            if (!(error instanceof SocketTimeoutException)) {
//                                throw new RuntimeException(error);
//                            }
                            mErrorCallback.onError();
                        });
    }

    private Statement subjectRequest() {
        Statement subject = new Statement();
        String st = Request.FIND_USED_SUBJECTS_BY_SCHOOL;
        subject.setStatement(st);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.NAME, mMember.getSchool().getName());
        subject.setParameters(parameters);
        return subject;
    }

    @Override
    public void onClick(String subject) {
        SharedPrefs.searchSubject(subject, getActivity());
//        mSearchRepeaterSingleton.startSearch();

        mSearchListener.onNext();
    }

}
