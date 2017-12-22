package ru.crew.motley.piideo.piideo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import ru.crew.motley.piideo.R;

/**
 * Created by vas on 12/22/17.
 */

public class PiideoActivity extends AppCompatActivity {

    private static final String TAG = PiideoActivity.class.getSimpleName();
    private static final String EXTRA_PIIDEO_NAME = "piideoName";

    public static Intent getIntent(Context context, String piideoName) {
        Intent i = new Intent(context, PiideoActivity.class);
        i.putExtra(EXTRA_PIIDEO_NAME, piideoName);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        String piideoName = getIntent().getStringExtra(EXTRA_PIIDEO_NAME);
        Fragment fragment = PhotoImageFragment.getInstance(piideoName);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
