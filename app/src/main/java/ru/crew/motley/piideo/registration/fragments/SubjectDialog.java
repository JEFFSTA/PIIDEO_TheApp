package ru.crew.motley.piideo.registration.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
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
import ru.crew.motley.piideo.registration.SubjectDialogListener;

/**
 * Created by vas on 2/17/18.
 */

public class SubjectDialog extends DialogFragment {

    private static final String TAG = SubjectDialog.class.getSimpleName();

    private static final String ARG_SCHOOL_NAME = "subjectSchoolName";

    @BindView(R.id.subject_name)
    EditText subjectName;

    private Unbinder mUnbinder;
    private SubjectDialogListener mListener;
    private String mSchoolName;

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
        loadSubjects();
        mSchoolName = getArguments().getString(ARG_SCHOOL_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_subject, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
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
                            fillSubjectItems(subjects);
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

    private void fillSubjectItems(List<Subject> subjects) {

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
