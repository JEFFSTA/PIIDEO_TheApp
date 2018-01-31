package ru.crew.motley.piideo.registration.adapter;

import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.crew.motley.piideo.registration.RegistrationListener;
import ru.crew.motley.piideo.registration.fragments.PhoneFragment;
import ru.crew.motley.piideo.registration.fragments.PhoneVerifyFragment;
import ru.crew.motley.piideo.registration.fragments.SubjectFragment;

/**
 * Created by vas on 12/17/17.
 */

public class SetupAdapter extends FragmentPagerAdapter {

    /**
     * Page order
     */
    @IntDef({Page.SUBJECT_PAGE, Page.PHONE_PAGE, Page.VERIFY_PAGE})
    private @interface Page {
        int PHONE_PAGE = 0;
        int SUBJECT_PAGE = 1;
        int VERIFY_PAGE = 2;
    }

    private static final int PAGE_COUNT = 3;

    private RegistrationListener mRegistrationListener;

    public SetupAdapter(FragmentManager fm) {
        super(fm);
    }

    public SetupAdapter(FragmentManager fm, RegistrationListener registrationListener) {
        this(fm);
        mRegistrationListener = registrationListener;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(@Page int position) {
        Fragment fragment;
        switch (position) {
            case Page.SUBJECT_PAGE:
//                fragment = SubjectFragment.newInstance(null, mRegistrationListener);
                break;
            case Page.PHONE_PAGE:
//                fragment = PhoneFragment.newInstance(mRegistrationListener);
                break;
            case Page.VERIFY_PAGE:
//                fragment = PhoneVerifyFragment.newInstance(mRegistrationListener);
                break;
            default:
                throw new IllegalArgumentException("Setup page position is not in range.");
        }
        return null;
    }
}
