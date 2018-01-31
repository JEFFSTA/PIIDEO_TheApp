package ru.crew.motley.piideo.registration.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.parceler.Parcels;

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

import static android.content.ContentValues.TAG;

/**
 * Created by vas on 12/17/17.
 */

public class PhoneVerifyFragment extends ButterFragment {

    private static final String ARG_MEMBER = "member";

    private RegistrationListener mRegistrationListener;

    @BindView(R.id.finish_registration)
    Button mNext;
    @BindView(R.id.verificationCode)
    EditText mVerificationCode;

    private Member mMember;

    public static PhoneVerifyFragment newInstance(Parcelable member, RegistrationListener listener) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEMBER, member);
        PhoneVerifyFragment fragment = new PhoneVerifyFragment();
        fragment.mRegistrationListener = listener;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMember = Parcels.unwrap(getArguments().getParcelable(ARG_MEMBER));
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
//        Statements statements = new Statements();
//        statements.getValues().add(subjectRequest());
//        NeoApi api = NeoApiSingleton.getInstance();
//        api.executeStatement(statements)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(transaction -> {
//                            mRegistrationListener.onNextStep(mMember);
//                        },
//                        error -> {
//                            Log.e(TAG, "Registration wipe have been failed.", error);
//                        });

        verifyCode();
    }

    private Statement subjectRequest() {
        Statement statement = new Statement();
        statement.setStatement(Request.STUDIES_NOTHING);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
        statement.setParameters(parameters);
        return statement;
    }

    public void verifyCode() {
        String verificationId = SharedPrefs.getVerificationId(getActivity());
        String code = mVerificationCode.getText().toString().trim();
        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        FirebaseUser user = task.getResult().getUser();
                        // ...
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                    }
                });
    }
}
