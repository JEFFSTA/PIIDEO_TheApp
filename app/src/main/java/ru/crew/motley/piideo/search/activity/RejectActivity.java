package ru.crew.motley.piideo.search.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.splash.SplashActivity;

/**
 * Created by vas on 3/15/18.
 */

public class RejectActivity extends AppCompatActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, RejectActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_no_help);
        ((TextView) findViewById(R.id.topic)).setText(SharedPrefs.getRequestMessage(this));
        ((TextView) findViewById(R.id.subject)).setText(SharedPrefs.getSearchSubject(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SharedPrefs.getRejectVisited(this) == SharedPrefs.REJECT_VISITED) {
            Intent i = SplashActivity.getIntent(this);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = SplashActivity.getIntent(this);
        startActivity(i);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            finishAndRemoveTask();
//        } else {
        finish();
        SharedPrefs.setRejectVisited(SharedPrefs.REJECT_VISITED, this);
//        }
    }
}
