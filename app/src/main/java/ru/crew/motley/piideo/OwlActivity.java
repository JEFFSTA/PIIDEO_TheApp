package ru.crew.motley.piideo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import ru.crew.motley.piideo.registration.activity.UserSetupActivity;

/**
 * Created by vas on 3/11/18.
 */

public class OwlActivity extends AppCompatActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, OwlActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_owl);
        findViewById(R.id.next_btn).setOnClickListener(v -> {
            Intent i = UserSetupActivity.getIntent(this);
            startActivity(i);
            finish();
        });
    }
}
