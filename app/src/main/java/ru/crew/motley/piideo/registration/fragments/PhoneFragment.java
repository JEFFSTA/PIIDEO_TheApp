package ru.crew.motley.piideo.registration.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Single;
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
import ru.crew.motley.piideo.network.neo.transaction.TransactionResponse;
import ru.crew.motley.piideo.registration.RegistrationListener;

/**
 * Created by vas on 12/17/17.
 */

public class PhoneFragment extends ButterFragment {

    private static final String TAG = PhoneFragment.class.getSimpleName();

    @BindView(R.id.phone_code)
    CountryCodePicker mCCP;
    @BindView(R.id.phone_number)
    EditText mPhoneNumber;
    @BindView(R.id.next_btn)
    Button mNext;

    private RegistrationListener mRegistrationListener;

    public static PhoneFragment newInstance(RegistrationListener registrationListener) {
        Bundle args = new Bundle();
        PhoneFragment fragment = new PhoneFragment();
        fragment.setArguments(args);
        fragment.mRegistrationListener = registrationListener;
        return fragment;
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
        if (validatePhone()) {
            String phoneNumber = mPhoneNumber.getText().toString().trim().replaceAll("\\D", "");
//            String phoneNumber = mCCP.getFullNumber().trim().replaceAll("\\D", "");
            SharedPrefs.memberPhone(phoneNumber, getActivity());
            loadMember(phoneNumber);
        }
    }

    private boolean validatePhone() {
        if (mPhoneNumber.getText().toString().trim().replaceAll("\\D", "").isEmpty()) {
            Toast.makeText(getActivity(), R.string.reg_phone_toast, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loadMember(String phoneNumber) {
        Statements statements = new Statements();
        Statement create = new Statement();
        create.setStatement(Request.FIND_PERSON);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, phoneNumber);
        create.setParameters(parameters);
        statements.getValues().add(create);
        NeoApi api = NeoApiSingleton.getInstance();
        Single<TransactionResponse> apiRequest = api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread());
        ifMemberExist(apiRequest);
        ifMemberNotExist(apiRequest, phoneNumber);
    }

    private void ifMemberExist(Single<TransactionResponse> apiRequest) {
        apiRequest.filter(transaction -> isMemberExist(transaction))
                .map(transaction -> {
                    String json = getFirstRow(transaction);
                    return Member.fromJson(json);
                })
                .subscribe(
                        member -> {
                            Log.d(TAG, member.getPhoneNumber());
                            if (getActivity() != null) {
                                SharedPrefs.register(getActivity());
                                mRegistrationListener.onNextStep(member);
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "Error!: " + throwable.getLocalizedMessage());
                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                    .show();
                            throw new RuntimeException(throwable);
                        });
    }

    private void ifMemberNotExist(Single<TransactionResponse> apiRequest, String phoneNumber) {
        apiRequest.filter(transaction -> !isMemberExist(transaction))
                .map(transaction -> {
                    Member member = new Member();
                    member.setPhoneNumber(phoneNumber);
                    member.setRegistered(false);
                    return member;
                })
                .subscribe(
                        member -> {
                            if (getActivity() != null) {
                                mRegistrationListener.onNextStep(member);
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "Error!: " + throwable.getLocalizedMessage());
                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                    .show();
                            throw new RuntimeException(throwable);
                        });
    }

    private boolean isMemberExist(TransactionResponse transaction) {
        return !transaction.getResults().get(0).getData().isEmpty();
    }

    private String getFirstRow(TransactionResponse transaction) {
        return transaction.getResults()
                .get(0)
                .getData()
                .get(0)
                .getRow()
                .get(0)
                .getValue();
    }
}
