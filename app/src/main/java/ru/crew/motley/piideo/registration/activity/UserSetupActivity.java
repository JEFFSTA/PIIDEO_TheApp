package ru.crew.motley.piideo.registration.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.registration.RegistrationListener;
import ru.crew.motley.piideo.registration.fragments.PhoneFragment;
import ru.crew.motley.piideo.registration.fragments.PhoneVerifyFragment;
import ru.crew.motley.piideo.registration.fragments.SubjectFragment;
import ru.crew.motley.piideo.search.activity.SearchActivity;

/**
 * Created by vas on 12/17/17.
 */

public class UserSetupActivity extends AppCompatActivity implements RegistrationListener {

    private static final String TAG = UserSetupActivity.class.getSimpleName();


    /**
     * Page order
     */
    @IntDef({Page.SUBJECT_PAGE, Page.PHONE_PAGE, Page.VERIFY_PAGE, Page.COMPLETE})
    private @interface Page {
        int PHONE_PAGE = 0;
        int SUBJECT_PAGE = 1;
        int VERIFY_PAGE = 2;
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
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);
        showFragment(PhoneFragment.newInstance(this));
    }

    @Override
    public void onNextStep(Member member) {
        if (member != null) {
            mMember = member;
        }
        nextStep();
    }

    @Override
    public void onComplete() {
        Intent i = SearchActivity.getIntent(this);
        startActivity(i);
        finish();
    }

    private void nextStep() {
        switch (currentStep) {
            case Page.PHONE_PAGE:
                currentStep = Page.VERIFY_PAGE;
                break;
            case Page.VERIFY_PAGE:
                currentStep = Page.SUBJECT_PAGE;
                break;
            case Page.SUBJECT_PAGE:
                currentStep = Page.COMPLETE;
                return;
            default:
                throw new IllegalStateException("Page you want to showChat is unsupported");
        }
        showNextStep();
    }

    private void showNextStep() {
        switch (currentStep) {
            case Page.PHONE_PAGE:
                Fragment phone = PhoneFragment.newInstance(this);
                showFragment(phone);
                break;
            case Page.VERIFY_PAGE:
                Fragment verify = PhoneVerifyFragment.newInstance(this);
                showFragment(verify);
                break;
            case Page.SUBJECT_PAGE:
                Fragment subject = SubjectFragment.newInstance(mMember, this);
                showFragment(subject);
                return;
            case Page.COMPLETE:
                if (!mMember.isRegistered()) {
//                    createNewMember();
                } else {
                    showSearch();
                }
            default:
                throw new IllegalStateException("Page you want to showChat is unsupported");
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void showSearch() {
        startActivity(SearchActivity.getIntent(this));
        finish();
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_CONTACTS:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                    createContacts();
//                else
////                    showResponseDialog(getResources().getString(R.string.storage_permission_needed));
//                    break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//                break;
//        }
//    }


    private void showResponseDialog(String string) {
//        Snackbar.make(view, string, Snackbar.LENGTH_SHORT).showChat();
    }


}
