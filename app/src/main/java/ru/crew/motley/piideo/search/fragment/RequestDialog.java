package ru.crew.motley.piideo.search.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.search.RequestDialogCallback;

/**
 * Created by vas on 2/24/18.
 */

public class RequestDialog extends DialogFragment {

    private static final String TAG = RequestDialog.class.getSimpleName();

    @BindView(R.id.request_message)
    EditText requestMessage;

    private Unbinder mUnbinder;
    private DialogInterface.OnDismissListener mCallback;

    public static RequestDialog getInstance(DialogInterface.OnDismissListener callback) {
        RequestDialog dialog = new RequestDialog();
        Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.mCallback = callback;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.dialog_request, container, false);
//        mUnbinder = ButterKnife.bind(this, view);
//        return view;
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

//    @OnClick(R.id.send_request)
//    public void sendRequest() {
//        SharedPrefs.requestMessage(requestMessage.getText().toString(), getContext());
//        mCallback.onMessageInput();
//    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_request, null);
        mUnbinder = ButterKnife.bind(this, v);
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton("Send", (dialog, which) -> {
                    SharedPrefs.requestMessage(requestMessage.getText().toString(), getContext());
                    dismiss();
                    mCallback.onDismiss(getDialog());
                })
                .create();
    }

}
