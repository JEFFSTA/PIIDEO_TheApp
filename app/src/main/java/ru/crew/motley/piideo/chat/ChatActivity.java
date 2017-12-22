package ru.crew.motley.piideo.chat;

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

public class ChatActivity extends AppCompatActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, ChatActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Fragment fragment = ChatFragment.newInstance();
        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
