package ru.crew.motley.piideo.registration.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.parceler.Parcels;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.Subject;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.network.neo.transaction.Data;
import ru.crew.motley.piideo.network.neo.transaction.Result;
import ru.crew.motley.piideo.network.neo.transaction.Row;
import ru.crew.motley.piideo.registration.RegistrationListener;

import static ru.crew.motley.piideo.registration.fragments.PhoneFragment.FRENCH_LENGTH;
import static ru.crew.motley.piideo.registration.fragments.PhoneFragment.FRENCH_PREFIX;

/**
 * Created by vas on 12/17/17.
 */

public class SubjectFragment extends ButterFragment {

    private static final String TAG = SubjectFragment.class.getSimpleName();

    private static final String ARGS_MEMBER = "member";
    private static final int REQUEST_CONTACTS = 2;

    @BindView(R.id.next_btn)
    Button next;
    @BindView(R.id.subject)
    AutoCompleteTextView mSubject;

    private Member mMember;
    private Set<String> mPhones;
    private List<Subject> mSubjects;

    private RegistrationListener mRegistrationListener;

    public static SubjectFragment newInstance(Parcelable member, RegistrationListener registrationListener) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_MEMBER, member);
        SubjectFragment fragment = new SubjectFragment();
        fragment.setArguments(args);
        fragment.mRegistrationListener = registrationListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMember = Parcels.unwrap(getArguments().getParcelable(ARGS_MEMBER));
        mPhones = new HashSet<>();
        requestPermissionAndLoad();
        loadSubjects();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_subject;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        next.setEnabled(true);
        return v;
    }

    @OnClick(R.id.next_btn)
    public void finishRegistration() {
        next.setEnabled(false);
        String subject = mSubject.getText().toString().trim();
        if (subject.isEmpty()) {
            Toast.makeText(getActivity(), R.string.sch_subject_violation, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        SharedPrefs.memberSubject(subject, getActivity());
        setSubjectIfExist();
        deleteOldSubjectRelationAndCreateNewMember();
    }

    private void prepareAndCreateNewMember() {
        if (mMember.getSubject() == null) {
            createNewSubjectAndMember();
        } else {
            createNewMember();
        }
    }

    private void createNewMember() {
        NeoApi api = NeoApiSingleton.getInstance();
        Statements statements = new Statements();
        statements.getValues().add(meRequest());
        statements.getValues().add(studiesRequest());
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            Log.d(TAG,
                                    "User id" + transaction.getResults()
                                            .get(0)
                                            .getData()
                                            .get(0)
                                            .getMeta()
                                            .get(0)
                                            .getId());
                            long id = transaction.getResults()
                                    .get(0)
                                    .getData()
                                    .get(0)
                                    .getMeta()
                                    .get(0)
                                    .getId();
                            mMember.setId(id);
                            createContacts();
                            Log.d(TAG, "" + transaction.getResults().size());
                        },
                        error -> {
                            Log.e(TAG, "Error!: " + error.getLocalizedMessage());
                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                    .show();
                            if (!(error instanceof SocketTimeoutException)) {
                                throw new RuntimeException(error);
                            }
                        });
    }

    private void createContacts() {
        NeoApi api = NeoApiSingleton.getInstance();
        Statements statements1 = contactsRequests();
        if (statements1.getValues().isEmpty()) {
            mRegistrationListener.onComplete(mMember);
        } else {
            api.executeStatement(statements1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(transaction1 -> {
                                Log.d(TAG, "" + Arrays.toString(transaction1.getErrors().toArray(new Object[0])));
                                mRegistrationListener.onComplete(mMember);
                            },
                            error1 -> {
                                Log.e(TAG, "Error!: " + error1.getLocalizedMessage());
                                Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                        .show();
                                if (!(error1 instanceof SocketTimeoutException)) {
                                    throw new RuntimeException(error1);
                                }
                            });
        }
    }

    private void deleteOldSubjectRelationAndCreateNewMember() {
        Statements statements = new Statements();
        statements.getValues().add(noSubjectRequest());
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            prepareAndCreateNewMember();
                        },
                        error -> {
                            Log.e(TAG, "Deleteion old subjects problem occured", error);
                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                    .show();
                            if (!(error instanceof SocketTimeoutException)) {
                                throw new RuntimeException(error);
                            }
                        });
    }

    private void createNewSubjectAndMember() {
        Statements statements = new Statements();
        statements.getValues().add(subjectRequest());
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            Subject subject = Subject.fromJson(
                                    transaction.getResults()
                                            .get(0)
                                            .getData()
                                            .get(0)
                                            .getRow()
                                            .get(0)
                                            .getValue());
                            subject.setId(
                                    (long) transaction.getResults()
                                            .get(0)
                                            .getData()
                                            .get(0)
                                            .getMeta()
                                            .get(0)
                                            .getId());
                            mMember.setSubject(subject);
                            createNewMember();
                        },
                        error -> {
                            Log.e(TAG, "Neo Request exception", error);
                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                    .show();
                            if (!(error instanceof SocketTimeoutException)) {
                                throw new RuntimeException(error);
                            }
                        });
    }

    private void setSubjectIfExist() {
        for (Subject subject : mSubjects) {
            if (subject.getName().equals(mSubject.getText().toString().trim())) {
                mMember.setSubject(subject);
            }
        }
    }

    private void requestPermissionAndLoad() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            loadContactsPhones();
        else
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACTS);
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
//                    if (phone.startsWith("33")) {
//                        phone = phone.replaceFirst("33", "");
//                    } else if (phone.startsWith("7")) {
//                        phone = phone.replaceFirst("7", "");
//                    } else if (phone.startsWith("8")) {
//                        phone = phone.replaceFirst("8", "");
//                    }
                    mPhones.add(phone);
                    managedCursor.moveToNext();
                }
            }
        } finally {
            managedCursor.close();
        }
    }

    private void loadSubjects() {
        Statements statements = new Statements();
        statements.getValues().add(subjectsRequest());
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            mSubjects = new ArrayList<>();
                            Log.d(TAG, "" + transaction.toString());
                            for (Result result : transaction.getResults()) {
                                List<Data> data = result.getData();
                                for (Data datum : data) {
                                    List<Row> rows = datum.getRow();
                                    for (int i = 0; i < rows.size(); i++) {
                                        Subject subject = Subject.fromJson(rows.get(i).getValue());
                                        subject.setId((long) datum.getMeta().get(i).getId());
                                        mSubjects.add(subject);
                                    }
                                }
                            }
                            fillAutocomplete();
                        },
                        error -> {
                            Log.e(TAG, "Loading subjects while registration problem", error);
                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                    .show();
                            if (!(error instanceof SocketTimeoutException)) {
                                throw new RuntimeException(error);
                            }
                        });
    }

    private void fillAutocomplete() {
        List<String> subjects = new ArrayList<>();
        for (Subject subject : mSubjects) {
            subjects.add(subject.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, subjects);
        mSubject.setAdapter(adapter);
        mSubject.setThreshold(1);
//        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    loadContactsPhones();
//                    createContacts();
                else {
                    Toast.makeText(getActivity(),
                            "This is a key permission. " +
                                    "You can't use this app without it.",
                            Toast.LENGTH_SHORT)
                            .show();
                    new Handler().postDelayed(() ->
                                    requestPermissions(
                                            new String[]{Manifest.permission.READ_CONTACTS},
                                            REQUEST_CONTACTS),
                            1000);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private Statements contactsRequests() {
        Statements statements = new Statements();
        for (String phone : mPhones) {
            statements.getValues().add(contactRequest(phone));
            statements.getValues().add(knowsRequest(phone));
        }
        return statements;
    }

    private Statement contactRequest(String phone) {
        Statement request = new Statement();
        if (phone.length() == FRENCH_LENGTH && phone.startsWith(FRENCH_PREFIX)) {
            request.setStatement(Request.NEW_CONTACT_WITH_PHONE_PREFIX);
        } else if (phone.length() > 10) {
            request.setStatement(Request.NEW_CONTACT_WITH_COUNTRY_CODE);
        } else {
            request.setStatement(Request.NEW_CONTACT);
        }
        Parameters parameters = new Parameters();
        if (phone.length() == FRENCH_LENGTH && phone.startsWith(FRENCH_PREFIX)) {
            parameters.getProps().put(Request.Var.PH_PREFIX, phone.substring(0, 1));
            parameters.getProps().put(Request.Var.PHONE, phone.substring(1, phone.length()));

        } else if (phone.startsWith("33")) {
            parameters.getProps().put(Request.Var.C_CODE, phone.substring(0, 2));
            parameters.getProps().put(Request.Var.PHONE, phone.substring(2, phone.length()));
        } else if (phone.length() > 10) {
            parameters.getProps().put(Request.Var.C_CODE, phone.substring(0, phone.length() - 10));
            parameters.getProps().put(Request.Var.PHONE, phone.substring(phone.length() - 10, phone.length()));
        } else {
            parameters.getProps().put(Request.Var.PHONE, phone);
        }
        request.setParameters(parameters);
        return request;
    }

    private Statement knowsRequest(String phone) {
        Statement request = new Statement();
        String filteredPhone;
        if (phone.length() == FRENCH_LENGTH && phone.startsWith(FRENCH_PREFIX)) {
            filteredPhone = phone.substring(1, phone.length());
        } else if (phone.startsWith("33")) {
            filteredPhone = phone.substring(2, phone.length());
        } else if (phone.length() > 10) {
            filteredPhone = phone.substring(phone.length() - 10, phone.length());
        } else {
            filteredPhone = phone;
        }
        request.setStatement(Request.KNOWS);
        Parameters parameters = new Parameters();

        parameters.getProps().put(Request.Var.PHONE + "From", mMember.getPhoneNumber());
        parameters.getProps().put(Request.Var.PHONE + "To", filteredPhone);
        request.setParameters(parameters);
        return request;
    }

    private Statement meRequest() {
        // TODO: 1/19/18 uncomment next
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Statement request = new Statement();
        if (!TextUtils.isEmpty(mMember.getCountryCode()) && !TextUtils.isEmpty(mMember.getPhonePrefix())) {
            request.setStatement(Request.ME_WITH_CC_AND_PREFIX);
        } else if (!TextUtils.isEmpty(mMember.getCountryCode())) {
            request.setStatement(Request.ME_WITH_CC);
        } else if (!TextUtils.isEmpty(mMember.getPhonePrefix())) {
            request.setStatement(Request.ME_WITH_PREFIX);
        } else {
            throw new RuntimeException("Illegal Full Phone Number Format");
        }
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
        // TODO: 1/19/18 uncomment next
        parameters.getProps().put(Request.Var.CHAT_ID, userId);
        if (!TextUtils.isEmpty(mMember.getCountryCode())) {
            parameters.getProps().put(Request.Var.C_CODE, mMember.getCountryCode());
        }
        if (!TextUtils.isEmpty(mMember.getPhonePrefix())) {
            parameters.getProps().put(Request.Var.PH_PREFIX, mMember.getPhonePrefix());
        }
        request.setParameters(parameters);
        return request;
    }

    private Statement subjectRequest() {
        Statement subject = new Statement();
        subject.setStatement(Request.NEW_SUBJECT);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.NAME, mSubject.getText().toString().trim());
        parameters.getProps().put(Request.Var.NAME_2, mMember.getSchool().getName());
        subject.setParameters(parameters);
        return subject;
    }

    private Statement studiesRequest() {
        Statement studies = new Statement();
        studies.setStatement(Request.STUDIES);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
        parameters.getProps().put(Request.Var.NAME, mMember.getSubject().getName());
        parameters.getProps().put(Request.Var.ID, mMember.getSubject().getId());
        studies.setParameters(parameters);
        return studies;
    }

    private Statement subjectsRequest() {
        Statement statement = new Statement();
        statement.setStatement(Request.FIND_SUBJECTS_BY_SCHOOL);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.NAME, mMember.getSchool().getName());
        statement.setParameters(parameters);
        return statement;
    }

    private Statement noSubjectRequest() {
        Statement statement = new Statement();
        statement.setStatement(Request.STUDIES_NOTHING);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
        statement.setParameters(parameters);
        return statement;
    }
}
