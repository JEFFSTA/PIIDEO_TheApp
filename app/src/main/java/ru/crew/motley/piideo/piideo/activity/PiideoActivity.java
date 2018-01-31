package ru.crew.motley.piideo.piideo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.piideo.fragment.PhotoImageFragment;

/**
 * Created by vas on 12/22/17.
 */

public class PiideoActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    private static final String TAG = PiideoActivity.class.getSimpleName();
    private static final String EXTRA_PIIDEO_NAME = "piideoName";
    private static final String EXTRA_MESSAGE = "fcm_message";
    private static final String EXTRA_MESSAGE_ID = "local_db_id";

    @Inject
    DispatchingAndroidInjector<Fragment> injector;

    public static Intent getIntent(Context context, String piideoName, Parcelable message, String dbMessageId) {
        Intent i = new Intent(context, PiideoActivity.class);
        i.putExtra(EXTRA_MESSAGE, message);
        i.putExtra(EXTRA_PIIDEO_NAME, piideoName);
        i.putExtra(EXTRA_MESSAGE_ID, dbMessageId);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_search);
        Parcelable message = getIntent().getParcelableExtra(EXTRA_MESSAGE);
        String piideoName = getIntent().getStringExtra(EXTRA_PIIDEO_NAME);
        String dbMessageId = getIntent().getStringExtra(EXTRA_MESSAGE_ID);
        Fragment fragment = PhotoImageFragment.getInstance(piideoName, message, dbMessageId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return injector;
    }
}
