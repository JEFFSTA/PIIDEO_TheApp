package ru.crew.motley.piideo.registration.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.crew.motley.piideo.R;
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
import ru.crew.motley.piideo.registration.SubjectAdapterListener;
import ru.crew.motley.piideo.registration.SubjectDialogListener;
import ru.crew.motley.piideo.registration.adapter.SubjectAdapter;

/**
 * Created by vas on 2/17/18.
 */

public class SubjectDialog extends DialogFragment implements SubjectAdapterListener {

    private static final String TAG = SubjectDialog.class.getSimpleName();

    private static final String ARG_SCHOOL_NAME = "subjectSchoolName";

    @BindView(R.id.subject_name)
    EditText subjectName;
    @BindView(R.id.subject_items)
    RecyclerView mSubjectsRecycler;

    private Unbinder mUnbinder;
    private SubjectDialogListener mListener;
    private String mSchoolName;
    private List<Subject> mSubjects;
//    private List<String> mSubjectNames;

    public static SubjectDialog getInstance(String schoolName, SubjectDialogListener listener) {
        SubjectDialog dialog = new SubjectDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SCHOOL_NAME, schoolName);
        dialog.setArguments(args);
        dialog.mListener = listener;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSchoolName = getArguments().getString(ARG_SCHOOL_NAME);
        mSubjects = new ArrayList<>();
        loadSubjects();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_subject, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mSubjectsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mSubjectsRecycler.setAdapter(new SubjectAdapter(mSubjects, this));
        subjectName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                List<Subject> containsPattern = new ArrayList<>();
                for (Subject subject : mSubjects) {
                    if (subject.getName().toLowerCase().startsWith(s.toString().toLowerCase().trim())) {
                        containsPattern.add(subject);
                    }
                }
                Collections.sort(containsPattern, ((o1, o2) ->
                        o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase())
                ));
                show(containsPattern);
            }
        });
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        subjectName.setOnEditorActionListener((v, actionId, event) -> {
            String newSubjectName = subjectName.getText().toString();
            if (actionId == EditorInfo.IME_ACTION_DONE && !newSubjectName.isEmpty()) {
                Subject newSubject = findOrCreateSubject(newSubjectName);
                onSubjectSelected(newSubject);
                InputMethodManager imm = (InputMethodManager) v.getContext().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;

        });
        return view;
    }

    private Subject findOrCreateSubject(String subjectName) {
        for (Subject subject : mSubjects) {
            if (subject.getName().toLowerCase().equals(subjectName.toLowerCase())) {
                return subject;
            }
        }
        Subject newSubject = new Subject();
        newSubject.setName(subjectName);
        return newSubject;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onSubjectSelected(Subject subject) {
        getDialog().dismiss();
        //
        mListener.onSubjectSelected(subject);
    }

    private void show(List<Subject> subjects) {
        if (mSubjectsRecycler != null) {
            ((SubjectAdapter) mSubjectsRecycler.getAdapter()).updateWithUI(subjects);
        }
    }

    private void loadSubjects() {
        Statements statements = new Statements();
        statements.getValues().add(subjectsRequest());
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            List<Subject> subjects = new ArrayList<>();
                            Log.d(TAG, "" + transaction.toString());
                            for (Result result : transaction.getResults()) {
                                List<Data> data = result.getData();
                                for (Data datum : data) {
                                    List<Row> rows = datum.getRow();
                                    for (int i = 0; i < rows.size(); i++) {
                                        Subject subject = Subject.fromJson(rows.get(i).getValue());
                                        subject.setId((long) datum.getMeta().get(i).getId());
                                        subjects.add(subject);
                                    }
                                }
                            }
                            mSubjects = subjects;
                            show(mSubjects);
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

    private Statement subjectsRequest() {
        Statement statement = new Statement();
        statement.setStatement(Request.FIND_SUBJECTS_BY_SCHOOL);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.NAME, mSchoolName);
        statement.setParameters(parameters);
        return statement;
    }

}
