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
        Member member = lab.getMember();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // TODO: 1/19/18 swap block comment/uncomment
        if (user == null || member == null) {
            new Handler().postDelayed(this::startRegistration, 2500);
        } else {
            skipRegistration(member);
        }
    }

    private void startRegistration() {
        Intent i = OwlActivity.getIntent(this);
        startActivity(i);
        finish();
    }

    private void skipRegistration(Member member) {
        Parcelable byPass = Parcels.wrap(member);
        String chatMessageId = SharedPrefs.loadChatMessageId(this);
        Log.d(TAG, "chat msg " + chatMessageId);
        if (SharedPrefs.isSearching(this)) {
            Intent i = SearchActivity.getIntent(byPass, this);
            startActivity(i);
        } else if (chatMessageId != null) {
            Intent i = ChatActivity.getIntent(chatMessageId, this);
            startActivity(i);
        } else {
            Intent i = SearchActivity.getIntent(byPass, this);
            startActivity(i);
        }
        finish();
    }
}
