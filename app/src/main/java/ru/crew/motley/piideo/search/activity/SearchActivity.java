package ru.crew.motley.piideo.search.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.ChatActivity;
import ru.crew.motley.piideo.search.SearchListener;
import ru.crew.motley.piideo.search.fragment.SearchResultFragment;
import ru.crew.motley.piideo.search.fragment.SearchSubjectFragment;

/**
 * Created by vas on 12/18/17.
 */

public class SearchActivity extends AppCompatActivity implements SearchListener{

    public static Intent getIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Fragment fragment = SearchSubjectFragment.newInstance(this);
        showFragment(fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mneu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.chat:
                Intent i = ChatActivity.getIntent(this);
                startActivity(i);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onNext() {
        Fragment fragment = SearchResultFragment.newInstance();
        showFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof SearchResultFragment) {
            fragment = SearchSubjectFragment.newInstance(this);
            showFragment(fragment);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBack() {

    }
}
