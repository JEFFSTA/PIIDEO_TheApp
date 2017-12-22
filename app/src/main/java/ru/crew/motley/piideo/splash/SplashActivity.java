package ru.crew.motley.piideo.splash;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by vas on 12/18/17.
 */

public class SplashActivity extends AppCompatActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, SplashActivity.class);
    }
}
