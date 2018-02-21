package ru.crew.motley.piideo.registration.fragments;

import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.registration.RegistrationListener;

/**
 * Created by vas on 12/17/17.
 */

public class PhoneFragment extends ButterFragment {

    private static final String TAG = PhoneFragment.class.getSimpleName();
    private static final String ARG_MEMBER = "member";

    public static final int FRENCH_LENGTH = 10;
    public static final int MOROCCO_LENGTH = 10;
    public static final int NIGERIA_LENGTH = 11;
    public static final int ALGERIA_LENGTH = 10;
//    public static final int Tunisia
//    public static final int Ivory_Coast,
//    public static final int Guinea_Conakry
//    public static final int Togo
//    public static final int Ghana
//    public static final int Cameroun


    public static final String FRENCH_PREFIX = "0";
    public static final String MOROCCO_PREFIX = "0";
    public static final String NIGERIA_PREFIX = "0";
    public

    @BindView(R.id.phone_code)
    CountryCodePicker mCCP;
    @BindView(R.id.phone_number)
    EditText mPhoneNumber;
    @BindView(R.id.next_btn)
    Button mNext;
    @BindView(R.id.timer)
    TextView timerValue;

    private RegistrationListener mRegistrationListener;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId;

    private Member mMember;

    private String OS;
    private String SDK;
    private String GOO_S;
    private String MF;

    public static PhoneFragment newInstance(/*Parcelable member,*/ RegistrationListener registrationListener) {
//        if (member == null) {
//            throw new NullPointerException("Member variable can't be null");
//        }
        Bundle args = new Bundle();
//        args.putParcelable(ARG_MEMBER, member);
        PhoneFragment fragment = new PhoneFragment();
        fragment.setArguments(args);
        fragment.mRegistrationListener = registrationListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            String vN = getContext().getPackageManager().getPackageInfo("com.google.android.gms", 0).versionName;
            OS = "Android " + Build.VERSION.RELEASE;
            SDK = "SDK " + Build.VERSION.SDK_INT;
            GOO_S = vN;
            MF = Build.MANUFACTURER;
            Log.d("FOR_AUTH", " " + vN);
            Log.d("FOR_AUTH",
                    Build.MANUFACTURER + " " +
                            Build.MODEL + " " +
                            "SDK " + Build.VERSION.SDK_INT + " " +
                            "Android " + Build.VERSION.RELEASE);
        } catch (PackageManager.NameNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
                mNext.setEnabled(true);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

//                if (e instanceof FirebaseAuthInvalidCredentialsException) {
//                    throw new RuntimeException(e);
//                } else if (e instanceof FirebaseTooManyRequestsException) {
//                    throw new RuntimeException(e);
//                }
                mNext.setEnabled(true);
                throw new RuntimeException(e);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
                SharedPrefs.verificationId(verificationId, getActivity());
            }
        };


        //todo replace back
        mMember = new Member();
        mMember.setRegistered(false);
        //todo from next
//        mMember = ChatLab.get(getActivity()).getMember();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_phone;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mCCP.registerCarrierNumberEditText(mPhoneNumber);
        return v;
    }


    @OnClick(R.id.next_btn)
    public void nextStep() {

//        mMember = ChatLab.get(getActivity()).getMember();
//        if (mMember != null) {
//            mRegistrationListener.onNextStep(mMember);
//            return;
//        }


        if (validatePhone()) {
            mNext.setEnabled(false);
            String phoneNumber = getPhoneNumber();
//            SharedPrefs.memberPhone(phoneNumber, getActivity());
            String fullNumber = mCCP.getFullNumber();
            if (!fullNumber.startsWith("+")) {
                fullNumber = "+" + fullNumber;
            }
            mMember.setCountryCode(mCCP.getSelectedCountryCode());
            if (mCCP.getSelectedCountryCode().equals("33") && phoneNumber.length() == FRENCH_LENGTH && phoneNumber.startsWith(FRENCH_PREFIX)) {
                mMember.setPhonePrefix(FRENCH_PREFIX);
                mMember.setPhoneNumber(phoneNumber.substring(1, phoneNumber.length()));
            } else if (mCCP.getSelectedCountryCode().equals("212") && phoneNumber.length() == MOROCCO_LENGTH && phoneNumber.startsWith(MOROCCO_PREFIX)) {
                mMember.setPhonePrefix(MOROCCO_PREFIX);
                mMember.setPhoneNumber(phoneNumber.substring(1, phoneNumber.length()));
            } else if (mCCP.getSelectedCountryCode().equals("234") && phoneNumber.length() == NIGERIA_LENGTH && phoneNumber.startsWith(NIGERIA_PREFIX)) {
                mMember.setPhonePrefix(NIGERIA_PREFIX);
                mMember.setPhoneNumber(phoneNumber.substring(1, phoneNumber.length()));
            } else if (mCCP.getSelectedCountryCode().equals("213") && phoneNumber.length() == MOROCCO_LENGTH && phoneNumber.startsWith(MOROCCO_PREFIX)) {
                mMember.setPhonePrefix(MOROCCO_PREFIX);
                mMember.setPhoneNumber(phoneNumber.substring(1, phoneNumber.length()));
            } else {
                mMember.setPhoneNumber(phoneNumber);
            }
            // TODO: swap next 2 strings commented/uncommented
//            mRegistrationListener.onNextStep(mMember);
            Toast.makeText(getActivity(), mMember.getPhoneNumber(), Toast.LENGTH_LONG).show();
            login(fullNumber);
//            signIn();
//            mRegistrationListener.onNextStep(mMember);
        }
    }

    private String getPhoneNumber() {
        return mPhoneNumber.getText().toString().trim().replaceAll("\\D", "");
    }

    private boolean validatePhone() {
        if (mPhoneNumber.getText().toString().trim().replaceAll("\\D", "").isEmpty()) {
            Toast.makeText(getActivity(), R.string.reg_phone_toast, Toast.LENGTH_SHORT).show();
            return false;
        }
//        if (!mCCP.isValidFullNumber()) {
//            Toast.makeText(getActivity(), R.string.reg_phone_format, Toast.LENGTH_SHORT).show();
//            return false;
//        }
        String cc = mCCP.getSelectedCountryCode();
        if (cc.equals("234") && getPhoneNumber().length() != NIGERIA_LENGTH) {
            Resources res = getResources();
            String text = res.getString(R.string.reg_phone_length, NIGERIA_LENGTH);
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((cc.equals("212") || cc.equals("213") || (cc.equals("33"))) && getPhoneNumber().length() != FRENCH_LENGTH) {
            Resources res = getResources();
            String text = res.getString(R.string.reg_phone_length, FRENCH_LENGTH);
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

//    private void login(String fullNumber) {
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                fullNumber,
//                60L,
//                TimeUnit.SECONDS,
//                getActivity(),
//                mCallbacks);
//
//        Bundle params = new Bundle();
//        params.putString("phone", fullNumber);
//        params.putString("OS", OS);
//        params.putString("SDK", SDK);
//        params.putString("GMS", GOO_S);
//        params.putString("MF", MF);
//        FirebaseAnalytics.getInstance(getActivity()).logEvent("registration", params);
//
//
//        mTimer = new TimerDelay();
//        handler.postDelayed(mTimer, 1000);
//    }


    private void login(String fullNumber) {
        signInAnonymously();
    }

    private void signInAnonymously() {
        FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");
                        final FirebaseUser user = task.getResult().getUser();
                        if (user != null) {

                            Log.d(TAG, "Member uid " + user.getUid());
                            mMember.setChatId(user.getUid());
                            mRegistrationListener.onNextStep(mMember);
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    "Authentication failed",
                                    Toast.LENGTH_SHORT)
                                    .show();

                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        throw new RuntimeException(task.getException());
                    }
                });
    }


    Handler handler = new Handler();
    Runnable mTimer;

    private class TimerDelay implements Runnable {

        private int seconds = 60;

        @Override
        public void run() {
            if (seconds == 0) {
                handler.removeCallbacks(mTimer);
                if (timerValue != null && mNext != null) {
                    timerValue.setText("");
                    mNext.setEnabled(true);
                }
                return;
            }
            if (timerValue != null) {
                timerValue.setText("" + (seconds--) + " sec");
            }
            handler.postDelayed(this, 1000);
        }

    }

    private void signIn() {
        mRegistrationListener.onNextStep(mMember);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");

                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            Log.d(TAG, "Member uid " + user.getUid());
                            mMember.setChatId(user.getUid());
                            mRegistrationListener.onNextStep(mMember);
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    "Authentication failed",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(getContext(), "SignIn failure", Toast.LENGTH_SHORT).show();
//                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

//                        }
                    }
                });
    }


}
