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
import android.widget.Toast;

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick(R.id.send_request)
    public void sendRequest() {
        if (validate()) {
            SharedPrefs.requestMessage(requestMessage.getText().toString(), getContext());
            dismiss();
            mCallback.onDismiss(getDialog());
        }
    }

    private boolean validate() {
        if (requestMessage.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), R.string.req_dlg_empty_warn, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_request, null);
        mUnbinder = ButterKnife.bind(this, v);
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
    }

}
