package ru.crew.motley.piideo.registration.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;

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
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.NetworkErrorCallback;
import ru.crew.motley.piideo.network.Subject;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.registration.RegistrationListener;
import ru.crew.motley.piideo.registration.SubjectDialogListener;

import static ru.crew.motley.piideo.registration.fragments.PhoneFragment.FRENCH_LENGTH;
import static ru.crew.motley.piideo.registration.fragments.PhoneFragment.FRENCH_PREFIX;
import static ru.crew.motley.piideo.registration.fragments.PhoneFragment.MOROCCO_LENGTH;
import static ru.crew.motley.piideo.registration.fragments.PhoneFragment.MOROCCO_PREFIX;
import static ru.crew.motley.piideo.registration.fragments.PhoneFragment.NIGERIA_LENGTH;
import static ru.crew.motley.piideo.registration.fragments.PhoneFragment.NIGERIA_PREFIX;


public class SubjectFragment extends ButterFragment implements SubjectDialogListener {

    private static final String TAG = SubjectFragment.class.getSimpleName();

    private static final String ARGS_MEMBER = "member";
//    private static final int REQUEST_CONTACTS = 2;

    @BindView(R.id.next_btn)
    Button next;
    @BindView(R.id.subject)
    TextView mSubject;
    @BindView(R.id.textView2)
    TextView infoQuestionText;
    @BindView(R.id.phone_code)
    CountryCodePicker mCCP;

    private Member mMember;
    private Set<String> mPhones;
//    private List<Subject> mSubjects;

    private RegistrationListener mRegistrationListener;
    private NetworkErrorCallback mErrorCallback;

    public static SubjectFragment newInstance(Parcelable member, RegistrationListener registrationListener, NetworkErrorCallback errorCallback) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_MEMBER, member);
        SubjectFragment fragment = new SubjectFragment();
        fragment.setArguments(args);
        fragment.mRegistrationListener = registrationListener;
        fragment.mErrorCallback = errorCallback;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMember = Parcels.unwrap(getArguments().getParcelable(ARGS_MEMBER));
        mPhones = new HashSet<>();
//        requestPermissionAndLoad();
//        loadSubjects();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_subject;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        next.setEnabled(true);
        Resources res = getResources();
        String subText;
        if (mMember.getSchool().getName().equals("High School")) {
            subText = res.getString(R.string.reg_trimestre);
        } else {
            subText = res.getString(R.string.reg_semestre);
        }
        String text = res.getString(R.string.reg_subject_caption, subText);
        infoQuestionText.setText(text);

        return v;
    }

    @OnClick(R.id.subject)
    public void showSubjectDialog() {
        SubjectDialog.getInstance(mMember.getSchool().getName(), this)
                .show(getChildFragmentManager(), "dialog");
    }

    @Override
    public void onSubjectSelected(Subject subject) {
        mSubject.setText(subject.getName());
        mMember.setSubject(subject);
    }

    @OnClick(R.id.next_btn)
    public void finishRegistration() {

//        int permissionCheck = ContextCompat.checkSelfPermission(
//                getActivity(),
//                Manifest.permission.READ_CONTACTS);
//        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACTS);
//            return;
//        }

        if (mMember.getSubject() == null || TextUtils.isEmpty(mMember.getSubject().getName())) {
            Toast.makeText(getActivity(), R.string.sch_subject_violation, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        next.setEnabled(false);
//        deleteOldSubjectRelationAndCreateNewMember();
        prepareAndCreateNewMember();
    }

    private void prepareAndCreateNewMember() {
        mMember.setCountryCode(mCCP.getSelectedCountryCode());
        if (mMember.getSubject().getId() == null) {
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
                            mErrorCallback.onError();
                        });
    }

    private void createContacts() {
//        NeoApi api = NeoApiSingleton.getInstance();
//        Statements statements1 = contactsRequests();
//        if (statements1.getValues().isEmpty()) {
        mRegistrationListener.onComplete(mMember);
//        } else {
//            api.executeStatement(statements1)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(transaction1 -> {
//                                Log.d(TAG, "" + Arrays.toString(transaction1.getErrors().toArray(new Object[0])));
//                                mRegistrationListener.onComplete(mMember);
//                            },
//                            error1 -> {
//                                Log.e(TAG, "Error!: " + error1.getLocalizedMessage());
//                                Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
//                                        .show();
//                                mErrorCallback.onError();
//                            });
//        }
    }

//    private void deleteOldSubjectRelationAndCreateNewMember() {
//        Statements statements = new Statements();
//        statements.getValues().add(noSubjectRequest());
//        NeoApi api = NeoApiSingleton.getInstance();
//        api.executeStatement(statements)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(transaction -> {
//                            prepareAndCreateNewMember();
//                        },
//                        error -> {
//                            Log.e(TAG, "Deleteion old subjects problem occured", error);
//                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
//                                    .show();
//                            mErrorCallback.onError();
//                        });
//    }

//    private void requestPermissionAndLoad() {
//        int permissionCheck = ContextCompat.checkSelfPermission(
//                getActivity(),
//                Manifest.permission.READ_CONTACTS);
//        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
//            loadContactsPhones();
//        else
//            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACTS);
//    }

//    private void loadContactsPhones() {
//        Cursor managedCursor = getActivity().getContentResolver()
//                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                        new String[]{
//                                ContactsContract.CommonDataKinds.Phone._ID,
//                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//                                ContactsContract.CommonDataKinds.Phone.NUMBER},
//                        null,
//                        null,
//                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
//        List<String> phones = new ArrayList<>();
//        try {
//            if (managedCursor != null && managedCursor.getCount() > 0) {
//                managedCursor.moveToFirst();
//                while (!managedCursor.isAfterLast()) {
//                    String phone = managedCursor.getString(managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                    phones.add(phone);
//                    managedCursor.moveToNext();
//                }
//            }
//        } finally {
//            managedCursor.close();
//        }
//        for (String phone : phones) {
//            mPhones.add(phone.replaceAll("\\D", ""));
//        }
//    }

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

    private Statement meRequest() {
        // TODO: 1/19/18 uncomment next
        String userId = FirebaseAuth.getInstance().getUid();
        Statement request = new Statement();
//        if (!TextUtils.isEmpty(mMember.getCountryCode()) && !TextUtils.isEmpty(mMember.getPhonePrefix())) {
//            request.setStatement(Request.ME_WITH_CC_AND_PREFIX);
//            Toast.makeText(getActivity(), "WITH CC AND 0" + " " + mMember.getPhoneNumber(), Toast.LENGTH_LONG).show();
//        } else if (!TextUtils.isEmpty(mMember.getCountryCode())) {
            request.setStatement(Request.ME_WITH_CC);
//            Toast.makeText(getActivity(), "WITH CC " + " " + mMember.getPhoneNumber(), Toast.LENGTH_LONG).show();
//        } else if (!TextUtils.isEmpty(mMember.getPhonePrefix())) {
//            request.setStatement(Request.ME_WITH_PREFIX);
//            Toast.makeText(getActivity(), "WITH 0" + " " + mMember.getPhoneNumber(), Toast.LENGTH_LONG).show();
//        } else {
//            throw new RuntimeException("Illegal Full Phone Number Format");
//        }
        Parameters parameters = new Parameters();
//        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
        // TODO: 1/19/18 uncomment next
        parameters.getProps().put(Request.Var.CHAT_ID, userId);
        if (!TextUtils.isEmpty(mMember.getCountryCode())) {
            parameters.getProps().put(Request.Var.C_CODE, mMember.getCountryCode());
        }
//        if (!TextUtils.isEmpty(mMember.getPhonePrefix())) {
//            parameters.getProps().put(Request.Var.PH_PREFIX, mMember.getPhonePrefix());
//        }
        request.setParameters(parameters);
        return request;
    }

    private Statement studiesRequest() {
        String chatId = FirebaseAuth.getInstance().getUid();
        Statement studies = new Statement();
        studies.setStatement(Request.STUDIES);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.CHAT_ID, chatId);
        parameters.getProps().put(Request.Var.NAME, mMember.getSubject().getName());
        parameters.getProps().put(Request.Var.ID, mMember.getSubject().getId());
        studies.setParameters(parameters);
        return studies;
    }

//    private Statement noSubjectRequest() {
//        Statement statement = new Statement();
//        statement.setStatement(Request.STUDIES_NOTHING);
//        Parameters parameters = new Parameters();
//        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
//        statement.setParameters(parameters);
//        return statement;
//    }

    private Statement subjectRequest() {
        Statement subject = new Statement();
        subject.setStatement(Request.NEW_SUBJECT);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.NAME, mSubject.getText().toString().trim());
        parameters.getProps().put(Request.Var.NAME_2, mMember.getSchool().getName());
        subject.setParameters(parameters);
        return subject;
    }
}
