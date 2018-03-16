package ru.crew.motley.piideo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import ru.crew.motley.piideo.registration.activity.UserSetupActivity;

/**
 * Created by vas on 3/11/18.
 */

public class OwlActivity extends AppCompatActivity {

    private static String TAG = OwlActivity.class.getSimpleName();

    private static final int REQUEST_SD_CARD = 666;

    public static Intent getIntent(Context context) {
        return new Intent(context, OwlActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_registration);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_owl);
        findViewById(R.id.next_btn).setOnClickListener(v -> {
//            List<String> permissions = requiredPermissions();
//            if (!permissions.isEmpty())
//                ActivityCompat.requestPermissions(
//                        this,
//                        permissions.toArray(new String[0]),
//                        REQUEST_SD_CARD);
//            else {
                Log.d(TAG, " on next ");
                Intent i = UserSetupActivity.getIntent(this);
                startActivity(i);
                finish();
//            }
        });
    }

//    private List<String> requiredPermissions() {
//        int writePermission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
//        int readPermission = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
//        List<String> forRequest = new ArrayList<>();
//        if (writePermission != PERMISSION_GRANTED)
//            forRequest.add(WRITE_EXTERNAL_STORAGE);
//        if (readPermission != PERMISSION_GRANTED)
//            forRequest.add(READ_EXTERNAL_STORAGE);
//        return forRequest;
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_SD_CARD)
//            if (grantResults.length > 0 && validate(grantResults))
//                nextScreen();
//            else
//                showPersmissionToast();
//        else
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//
//    }

//    private boolean validate(int[] grantResults) {
//        for (int i : grantResults)
//            if (i == PERMISSION_DENIED) return false;
//        return true;
//    }

//    private void nextScreen() {
//        Intent i = UserSetupActivity.getIntent(this);
//        startActivity(i);
//        finish();
//    }

//    private void showPersmissionToast() {
//        Toast.makeText(this,
//                "This is a key permission. " + "You can't use this app without it.",
//                Toast.LENGTH_SHORT)
//                .show();
//    }

}

