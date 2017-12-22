package ru.crew.motley.piideo.registration.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.registration.RegistrationListener;

/**
 * Created by vas on 12/17/17.
 */

public class PhoneVerifyFragment extends ButterFragment {

    private RegistrationListener mRegistrationListener;

    @BindView(R.id.finish_registration)
    Button mNext;

    public static PhoneVerifyFragment newInstance(RegistrationListener listener) {
        Bundle args = new Bundle();
        PhoneVerifyFragment fragment = new PhoneVerifyFragment();
        fragment.mRegistrationListener = listener;
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_phone_verify;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        return v;
    }

    @OnClick(R.id.finish_registration)
    public void nextStep() {
        mRegistrationListener.onNextStep(null);
    }
}
