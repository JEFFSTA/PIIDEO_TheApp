package ru.crew.motley.piideo.settings.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.search.activity.SearchActivity;
import ru.crew.motley.piideo.settings.fragment.SubjectSettingsFragment;

/**
 * Created by vas on 3/7/18.
 */

public class SettingsActivity extends AppCompatActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        showSettings();
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        super.onCreate(savedInstanceState, persistentState);
//
//    }

    private void showSettings() {
        SubjectSettingsFragment fragment = new SubjectSettingsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

}
