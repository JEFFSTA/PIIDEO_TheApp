package ru.crew.motley.piideo.registration.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.NetworkErrorCallback;
import ru.crew.motley.piideo.network.activity.ConnectionErrorActivity;
import ru.crew.motley.piideo.registration.RegistrationListener;
import ru.crew.motley.piideo.registration.fragments.PhoneFragment;
import ru.crew.motley.piideo.registration.fragments.PhoneVerifyFragment;
import ru.crew.motley.piideo.registration.fragments.SchoolGroupFragment;
import ru.crew.motley.piideo.registration.fragments.SubjectFragment;
import ru.crew.motley.piideo.search.activity.SearchActivity;

/**
 * Created by vas on 12/17/17.
 */

public class UserSetupActivity extends ConnectionErrorActivity implements RegistrationListener, NetworkErrorCallback {

    private static final String TAG = UserSetupActivity.class.getSimpleName();


    /**
     * Page order
     */
    @IntDef({Page.SUBJECT_PAGE, Page.PHONE_PAGE, Page.VERIFY_PAGE, Page.SCHOOL_PAGE, Page.COMPLETE})
    private @interface Page {
        int PHONE_PAGE = 0;
        int SUBJECT_PAGE = 1;
        int VERIFY_PAGE = 2;
        int SCHOOL_PAGE = 3;
        int COMPLETE = 10;
    }

    private Member mMember;
    @Page
    private int currentStep;

    public static Intent getIntent(Context context) {
        return new Intent(context, UserSetupActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);
        if (mMember == null) {
            mMember = new Member();
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) {
        currentStep = Page.PHONE_PAGE;
//        } else {
//            currentStep = Page.SCHOOL_PAGE;
//        }
        showNextStep();
    }

    @Override
    public void onNextStep(Member member) {
        if (member != null) {
            mMember = member;
        }
        nextStep();
    }

    @Override
    public void onComplete(Member member) {
        ChatLab lab = ChatLab.get(this);
        if (member.getChatId() == null) {
            member.setChatId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
        lab.addMember(member);
        Parcelable byPass = Parcels.wrap(member);
        Intent i = SearchActivity.getIntent(byPass, this);
        startActivity(i);
        finish();
    }

    private void nextStep() {
        switch (currentStep) {
            case Page.PHONE_PAGE:
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    currentStep = Page.VERIFY_PAGE;
                } else {
                    currentStep = Page.SCHOOL_PAGE;
                }
                break;
            case Page.VERIFY_PAGE:
                currentStep = Page.SCHOOL_PAGE;
                break;
            case Page.SCHOOL_PAGE:
                currentStep = Page.SUBJECT_PAGE;
                break;
            case Page.SUBJECT_PAGE:
                throw new IllegalStateException("Page you want to go out is final, just use onComplete.");
//                currentStep = Page.COMPLETE;
//                return;
            default:
                throw new IllegalStateException("Page you want to showChat is unsupported. Current step is " + currentStep);
        }
        showNextStep();
    }

    private void showNextStep() {
        switch (currentStep) {
            case Page.PHONE_PAGE:
                Fragment phone = PhoneFragment.newInstance(this, this);
                showFragment(phone);
                break;
            case Page.VERIFY_PAGE:
                Parcelable memberForVerification = Parcels.wrap(mMember);
                Fragment verify = PhoneVerifyFragment.newInstance(memberForVerification, this, this);
                showFragment(verify);
                break;
            case Page.SCHOOL_PAGE:
                Parcelable memberForSchool = Parcels.wrap(mMember);
                Fragment school = SchoolGroupFragment.newInstance(memberForSchool, this, this);
                showFragment(school);
                break;
            case Page.SUBJECT_PAGE:
                Parcelable memberForSubject = Parcels.wrap(mMember);
                Fragment subject = SubjectFragment.newInstance(memberForSubject, this, this);
                showFragment(subject);
                break;
            case Page.COMPLETE:
                // do nothing
//                if (!mMember.isRegistered()) {
//                    createNewMember();
//                } else {
//                    showSearch();
//                }
            default:
                throw new IllegalStateException("Page you want to showChat is unsupported");
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (errorShown()) {
            backFromError();
        } else {
            onBackPressed();
        }
    }
}