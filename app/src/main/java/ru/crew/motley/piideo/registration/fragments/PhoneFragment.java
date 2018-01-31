package ru.crew.motley.piideo.registration.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.registration.RegistrationListener;

/**
 * Created by vas on 12/17/17.
 */

public class PhoneFragment extends ButterFragment {

    private static final String TAG = PhoneFragment.class.getSimpleName();
    private static final String ARG_MEMBER = "member";

    public static final int FRENCH_LENGTH = 10;
    public static final String FRENCH_PREFIX = "0";

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
//        mMember = Parcels.unwrap(getArguments().getParcelable(ARG_MEMBER));
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
                mNext.setEnabled(true);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }
                mNext.setEnabled(true);

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                SharedPrefs.verificationId(verificationId, getActivity());
//                loadMember(getPhoneNumber());
                // ...
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
            } else {
                mMember.setPhoneNumber(phoneNumber);
            }
            // TODO: swap next 2 strings commented/uncommented
//            mRegistrationListener.onNextStep(mMember);
            login(fullNumber);
//            signIn();
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
        return true;
    }

//    private void loadMember(String phoneNumber, String authUid) {
//        Statements statements = new Statements();
//        Statement create = new Statement();
//        create.setStatement(Request.FIND_PERSON);
//        Parameters parameters = new Parameters();
//        parameters.getProps().put(Request.Var.PHONE, phoneNumber);
//        create.setParameters(parameters);
//        statements.getValues().add(create);
//        NeoApi api = NeoApiSingleton.getInstance();
//        Single<TransactionResponse> apiRequest = api.executeStatement(statements)
//                .observeOn(AndroidSchedulers.mainThread());
//        ifMemberExist(apiRequest, authUid);
//        ifMemberNotExist(apiRequest, phoneNumber, authUid);
//    }

//    private void ifMemberExist(Single<TransactionResponse> apiRequest, String authUid) {
//        apiRequest.filter(transaction -> isMemberExist(transaction))
//                .map(transaction -> {
//                    String json = getFirstRow(transaction);
//                    Member member = Member.fromJson(json);
//                    member.setId(getRowId(transaction));
//                    member.setChatId(authUid);
//                    return member;
//                })
//                .subscribe(
//                        member -> {
//                            Log.d(TAG, member.getPhoneNumber());
//                            if (getActivity() != null) {
//                                SharedPrefs.register(getActivity());
//                                mRegistrationListener.onNextStep(member);
//                            }
//                        },
//                        throwable -> {
//                            Log.e(TAG, "Error!: " + throwable.getLocalizedMessage());
//                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
//                                    .show();
//                            throw new RuntimeException(throwable);
//                        });
//    }

//    private void ifMemberNotExist(Single<TransactionResponse> apiRequest, String phoneNumber, String authUid) {
//        apiRequest.filter(transaction -> !isMemberExist(transaction))
//                .map(transaction -> {
//                    Member member = new Member();
//                    member.setPhoneNumber(phoneNumber);
//                    member.setRegistered(false);
//                    member.setChatId(authUid);
//                    return member;
//                })
//                .subscribe(
//                        member -> {
//                            if (getActivity() != null) {
//                                mRegistrationListener.onNextStep(member);
//                            }
//                        },
//                        throwable -> {
//                            Log.e(TAG, "Error!: " + throwable.getLocalizedMessage());
//                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
//                                    .show();
//                            throw new RuntimeException(throwable);
//                        });
//    }

//    private boolean isMemberExist(TransactionResponse transaction) {
//        return !transaction.getResults().get(0).getData().isEmpty();
//    }

//    private String getFirstRow(TransactionResponse transaction) {
//        return transaction.getResults()
//                .get(0)
//                .getData()
//                .get(0)
//                .getRow()
//                .get(0)
//                .getValue();
//    }

//    private Long getRowId(TransactionResponse transaction) {
//        return (long) transaction.getResults()
//                .get(0)
//                .getData()
//                .get(0)
//                .getMeta()
//                .get(0)
//                .getId();
//    }

    private void login(String fullNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                fullNumber,
                60L,
                TimeUnit.SECONDS,
                getActivity(),
                mCallbacks);
        mTimer = new TimerDelay();
        handler.postDelayed(mTimer, 1000);
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
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            Log.d(TAG, "Member uid " + user.getUid());
//                            loadMember(getPhoneNumber(), user.getUid());
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
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                    }
                });
    }


}
