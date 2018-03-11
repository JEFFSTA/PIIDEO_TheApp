package ru.crew.motley.piideo.registration.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.parceler.Parcels;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.NetworkErrorCallback;
import ru.crew.motley.piideo.network.School;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.network.neo.transaction.Data;
import ru.crew.motley.piideo.network.neo.transaction.Result;
import ru.crew.motley.piideo.network.neo.transaction.Row;
import ru.crew.motley.piideo.network.neo.transaction.RowMeta;
import ru.crew.motley.piideo.network.neo.transaction.TransactionResponse;
import ru.crew.motley.piideo.registration.RegistrationListener;

/**
 * Created by vas on 1/3/18.
 */

public class SchoolGroupFragment extends ButterFragment {

    private static final String TAG = SchoolGroupFragment.class.getSimpleName();
    private static final String ARG_MEMBER = "member";

    @BindView(R.id.high_school)
    View highSchool;
    @BindView(R.id.high_school_active)
    ImageView highSchoolActive;
    @BindView(R.id.prep_classes)
    View prepClasses;
    @BindView(R.id.prep_classes_active)
    ImageView prepClassesActive;
    @BindView(R.id.engin_school)
    View enginSchool;
    @BindView(R.id.engin_school_active)
    ImageView enginSchoolActive;

    private RegistrationListener mRegistrationListener;
    private NetworkErrorCallback mErrorCallback;

    private Member mMember;
    private List<School> mSchoolGroups;

    public static SchoolGroupFragment newInstance(Parcelable member, RegistrationListener registrationListener, NetworkErrorCallback errorCallback) {
        if (member == null) {
            throw new NullPointerException("Member varible can't be null.");
        }
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEMBER, member);
        SchoolGroupFragment fragment = new SchoolGroupFragment();
        fragment.setArguments(args);
        fragment.mRegistrationListener = registrationListener;
        fragment.mErrorCallback = errorCallback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_school_group;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSchoolGroups();
        mMember = Parcels.unwrap(getArguments().getParcelable(ARG_MEMBER));
    }

    private void deactivateAll() {
        highSchoolActive.setImageResource(R.drawable.ic_school_group_gray);
        prepClassesActive.setImageResource(R.drawable.ic_school_group_gray);
        enginSchoolActive.setImageResource(R.drawable.ic_school_group_gray);
    }

    @OnClick(R.id.high_school)
    public void chooseHighSchool() {
        for (School school: mSchoolGroups) {
            if (school.getName().equals("High School")) {
                mMember.setSchool(school);
                deactivateAll();
                highSchoolActive.setImageResource(R.drawable.ic_school_group_green);
//                mRegistrationListener.onNextStep(mMember);
            }
        }
    }

    @OnClick(R.id.prep_classes)
    public void choosePreparatory() {
        for (School school: mSchoolGroups) {
            if (school.getName().equals("Preparatory Classes")) {
                mMember.setSchool(school);
                deactivateAll();
                prepClassesActive.setImageResource(R.drawable.ic_school_group_green);
//                mRegistrationListener.onNextStep(mMember);
            }
        }
    }

    @OnClick(R.id.engin_school)
    public void chooseEngineeringSchool() {
        for (School school: mSchoolGroups) {
            if (school.getName().equals("Engineering School")) {
                mMember.setSchool(school);
                deactivateAll();
                enginSchoolActive.setImageResource(R.drawable.ic_school_group_green);
//                mRegistrationListener.onNextStep(mMember);
            }
        }
    }

    @OnClick(R.id.next_btn)
    public void showNextStep() {
        mRegistrationListener.onNextStep(mMember);
    }

    private void loadSchoolGroups() {
        Statements statements = new Statements();
        statements.getValues().add(groupRequest());
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            Log.d(TAG, transaction.toString());
                            mSchoolGroups = getSchoolGroups(transaction);
                        },
                        error -> {
                            Log.e(TAG, "Load school groups failed");
                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                    .show();
                            mErrorCallback.onError();
//                            if (!(error instanceof SocketTimeoutException)) {
//                                throw new RuntimeException(error);
//                            }
                        });
    }

    private Statement groupRequest() {
        Statement statement = new Statement();
        statement.setStatement(Request.FIND_ALL_SCHOOL_GROUPS);
        return statement;
    }

    private List<School> getSchoolGroups(TransactionResponse transaction) {
        List<Result> results = transaction.getResults();
        List<School> schoolGroups = new ArrayList<>();
        for (Result result : results) {
            for (Data datum : result.getData()) {
                for (int i = 0; i < datum.getRow().size(); i++) {
                    School school = School.fromJson(datum.getRow().get(i).getValue());
                    school.setId((long) datum.getMeta().get(i).getId());
                    schoolGroups.add(school);
                }
            }
        }
        return schoolGroups;
    }
}
