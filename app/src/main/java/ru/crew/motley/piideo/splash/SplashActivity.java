package ru.crew.motley.piideo.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.parceler.Parcels;

import ru.crew.motley.piideo.OwlActivity;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.chat.activity.ChatActivity;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.registration.activity.UserSetupActivity;
import ru.crew.motley.piideo.search.activity.SearchActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private Member mMember;

    public static Intent getIntent(Context context) {
        return new Intent(context, SplashActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ChatLab lab = ChatLab.get(this);
        mMember = lab.getMember();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            signInAnonymously();
        } else if (mMember == null) {
            new Handler().postDelayed(this::startRegistration, 2500);
        } else {
            skipRegistration();
        }
    }

    private void signInAnonymously() {
        FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInAnonymously:success");
                        final FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            Log.d(TAG, "Member uid " + user.getUid());
                            new Handler().postDelayed(this::startRegistration, 1500);
                        } else {
                            Toast.makeText(
                                    this,
                                    "Authentication failed",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else {
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                    }
                })
                .addOnFailureListener(this, task -> {
//                    mNetworkErrorCallback.onError();
                });
    }

    private void startRegistration() {
        Intent i = OwlActivity.getIntent(this);
        startActivity(i);
        finish();
    }

    private void skipRegistration() {
        Parcelable byPass = Parcels.wrap(mMember);
        String chatMessageId = SharedPrefs.loadChatMessageId(this);
        Log.d(TAG, "chat msg " + chatMessageId);
        if (SharedPrefs.isSearching(this)) {
            Intent i = SearchActivity.getIntent(byPass, this);
            startActivity(i);
        } else if (SharedPrefs.loadChatIdleStartTime(this) != -1) {
            Intent i = ChatActivity.getIntent(chatMessageId, this);
            startActivity(i);
        } else {
            Intent i = SearchActivity.getIntent(byPass, this);
            startActivity(i);
        }
        finish();
    }
}
