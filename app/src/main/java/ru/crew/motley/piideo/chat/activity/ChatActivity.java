package ru.crew.motley.piideo.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.fragment.ChatFragment;
import ru.crew.motley.piideo.chat.fragment.WatchPiideoFragment;

public class ChatActivity extends AppCompatActivity
        implements
        ChatFragment.PiideoShower,
        WatchPiideoFragment.ChatShower,
        HasSupportFragmentInjector {

    private static String EXTRA_DB_MESSAGE_ID = "local_db_id";

    private String mDBMessageId;

    @Inject
    DispatchingAndroidInjector<Fragment> injector;


    public static Intent getIntent(String dbMessageId, Context context) {
        Intent i = new Intent(context, ChatActivity.class);
        i.putExtra(EXTRA_DB_MESSAGE_ID, dbMessageId);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mDBMessageId = getIntent().getStringExtra(EXTRA_DB_MESSAGE_ID);
        Fragment fragment = ChatFragment.newInstance(mDBMessageId, this);
        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void showPiideo(String piideoFileName) {
        Fragment fragment = WatchPiideoFragment.newInstance(this, piideoFileName);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showChat() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            Fragment fragment = ChatFragment.newInstance(mDBMessageId, this);
            showFragment(fragment);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else
            super.onBackPressed();
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return injector;
    }


}
