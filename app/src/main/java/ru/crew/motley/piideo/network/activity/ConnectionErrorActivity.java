package ru.crew.motley.piideo.network.activity;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.network.NetworkErrorCallback;
import ru.crew.motley.piideo.network.fragment.ConnectionErrorFragment;

/**
 * Created by vas on 3/11/18.
 */

public class ConnectionErrorActivity extends AppCompatActivity implements NetworkErrorCallback {

    protected boolean errorShown() {
        return getSupportFragmentManager().getBackStackEntryCount() > 0;
    }

    protected void backFromError() {
        getSupportFragmentManager().popBackStack();
    }


    @Override
    public void onError() {
        Fragment fragment = new ConnectionErrorFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
}
