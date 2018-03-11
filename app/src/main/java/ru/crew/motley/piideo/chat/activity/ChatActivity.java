package ru.crew.motley.piideo.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.fragment.ChatFragment;
import ru.crew.motley.piideo.chat.fragment.WatchPiideoFragment;
import ru.crew.motley.piideo.network.activity.ConnectionErrorActivity;

public class ChatActivity extends ConnectionErrorActivity
        implements
        ChatFragment.PiideoShower,
        WatchPiideoFragment.ChatShower,
        HasSupportFragmentInjector {

    private static final String TAG = ChatActivity.class.getSimpleName();

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
        super.onCreate(savedInstanceState);
        Log.d(TAG, "create Activity ");
        setContentView(R.layout.activity_chat);
        mDBMessageId = getIntent().getStringExtra(EXTRA_DB_MESSAGE_ID);
        Fragment fragment = ChatFragment.newInstance(mDBMessageId, this, this);
        showFragment(fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "resume Activity ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "pause Activity ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destroy Activity ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stop Activity ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start Activity ");
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
            Fragment fragment = ChatFragment.newInstance(mDBMessageId, this, this);
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
