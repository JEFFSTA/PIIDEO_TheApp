package ru.crew.motley.piideo.settings.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.SocketTimeoutException;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.Subject;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.registration.SubjectDialogListener;
import ru.crew.motley.piideo.registration.fragments.SubjectDialog;

/**
 * Created by vas on 3/7/18.
 */

public class SubjectSettingsFragment extends ButterFragment implements SubjectDialogListener {

    private static final String TAG = SubjectSettingsFragment.class.getSimpleName();

    private Member mMember;

    @BindView(R.id.new_subject)
    TextView mNewSubjectText;
    @BindView(R.id.current_subject)
    TextView mCurrentSubject;
    @BindView(R.id.save_btn)
    Button mSaveButton;

    public static SubjectSettingsFragment newInstance() {
        Bundle args = new Bundle();
        SubjectSettingsFragment fragment = new SubjectSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMember = ChatLab.get(getContext()).getMember();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_settings;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCurrentSubject();
    }

    @Override
    public void onSubjectSelected(Subject subject) {
        mMember.setSubject(subject);
        String firstCapitalized = subject.getName()
                .substring(0, 1)
                .toUpperCase();
        String subjectName = subject.getName().replaceFirst("^.", firstCapitalized);
        mNewSubjectText.setText(subjectName);
    }

    @OnClick(R.id.save_btn)
    public void saveNewSubject() {
        setEnabledUI(false);
        deleteOldSubjectAndCreateNew();
    }

    private void setCurrentSubject() {
        String firstCapitalized = mMember.getSubject()
                .getName()
                .substring(0, 1)
                .toUpperCase();
        String subjectName = mMember.getSubject()
                .getName()
                .replaceFirst("^.", firstCapitalized);
        mCurrentSubject.setText(subjectName);
    }

    private void setEnabledUI(boolean state) {
        mSaveButton.setEnabled(state);
        mNewSubjectText.setEnabled(state);
    }

    @OnClick(R.id.new_subject)
    public void showSubjectDialog() {
        SubjectDialog.getInstance(mMember.getSchool().getName(), this)
                .show(getChildFragmentManager(), "dialog");
    }

    private void saveNewSubjectInDB() {
        ChatLab.get(getContext()).updateMember(mMember);
    }

    private void connectWithNewSubject() {
        NeoApi api = NeoApiSingleton.getInstance();
        Statements statements = new Statements();
        statements.getValues().add(studiesRequest());
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            Log.d(TAG,
                                    "User id" + transaction.getResults().toString());
                            setEnabledUI(true);
                            setCurrentSubject();
                            saveNewSubjectInDB();
                        },
                        error -> {
                            Log.e(TAG, "Error!: " + error.getLocalizedMessage());
                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                    .show();
                            if (!(error instanceof SocketTimeoutException)) {
                                throw new RuntimeException(error);
                            }
                            setEnabledUI(true);
                        });
    }

    private void deleteOldSubjectAndCreateNew() {
        Statements statements = new Statements();
        statements.getValues().add(noSubjectRequest());
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            connectWithNewSubject();
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

    private Statement noSubjectRequest() {
        Statement statement = new Statement();
        statement.setStatement(Request.STUDIES_NOTHING);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
        statement.setParameters(parameters);
        return statement;
    }
}
