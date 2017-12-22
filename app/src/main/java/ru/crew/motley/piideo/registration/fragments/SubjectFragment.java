package ru.crew.motley.piideo.registration.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.parceler.Parcels;

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
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.registration.RegistrationListener;

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
    EditText editText;

    private Member mMember;
    private Set<String> mPhones;

    private RegistrationListener mRegistrationListener;

    public static SubjectFragment newInstance(Member member, RegistrationListener registrationListener) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_MEMBER, Parcels.wrap(member));
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_subject;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnClick(R.id.next_btn)
    public void finishRegistration() {
        if (editText.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), R.string.sch_subject_violation, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        SharedPrefs.memberSubject(editText.getText().toString(), getActivity());
        createNewMember();
        mRegistrationListener.onNextStep(mMember);
    }

    private void createNewMember() {
        NeoApi api = NeoApiSingleton.getInstance();
        Statements statements = new Statements();
        statements.getValues().add(meRequest());
        statements.getValues().add(subjectRequest());
        statements.getValues().add(studiesRequest());
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            Statements statements1 = contactsRequests();
                            api.executeStatement(statements1)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(transaction1 -> mRegistrationListener.onComplete(),
                                            error1 -> {
                                                Log.e(TAG, "Error!: " + error1.getLocalizedMessage());
                                                Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                                        .show();
                                            });
                            Log.d(TAG, "" + transaction.getResults().size());
                        },
                        error -> {
                            Log.e(TAG, "Error!: " + error.getLocalizedMessage());
                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                    .show();
                        });
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
                    mPhones.add(phone);
                    managedCursor.moveToNext();
                }
            }
        } finally {
            managedCursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    loadContactsPhones();
//                    createContacts();
//                else
//                    showResponseDialog(getResources().getString(R.string.storage_permission_needed));
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
        request.setStatement(Request.NEW_CONTACT);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, phone);
        request.setParameters(parameters);
        return request;
    }

    private Statement knowsRequest(String phone) {
        Statement request = new Statement();
        request.setStatement(Request.KNOWS);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE + "From", phone);
        parameters.getProps().put(Request.Var.PHONE + "To", mMember.getPhoneNumber());
        request.setParameters(parameters);
        return request;
    }

    private Statement meRequest() {
        Statement request = new Statement();
        request.setStatement(Request.ME);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
        request.setParameters(parameters);
        return request;
    }

    private Statement subjectRequest() {
        Statement subject = new Statement();
        subject.setStatement(Request.NEW_SUBJECT);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.NAME, editText.getText().toString());
        subject.setParameters(parameters);
        return subject;
    }

    private Statement studiesRequest() {
        Statement studies = new Statement();
        studies.setStatement(Request.STUDIES);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
        parameters.getProps().put(Request.Var.NAME, editText.getText().toString());
        studies.setParameters(parameters);
        return studies;
    }
}
