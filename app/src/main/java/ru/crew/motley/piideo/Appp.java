package ru.crew.motley.piideo;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import io.fabric.sdk.android.Fabric;
import ru.crew.motley.piideo.di.DaggerMainComponent;
import ru.crew.motley.piideo.util.Utils;


public class Appp extends MultiDexApplication implements HasActivityInjector, HasServiceInjector {

    @Inject
    DispatchingAndroidInjector<Activity> mActivityInjector;

    @Inject
    DispatchingAndroidInjector<Service> mServiceInjector;

    private boolean searchActivityVisible = false;
    private boolean chatActivityVisible = false;
    private boolean handshakeActivityVisible = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference().keepSynced(true);
        DaggerMainComponent.builder()
                .application(this)
                .build()
                .inject(this);
    }

    public void searchActivityResumed() {
        searchActivityVisible = true;
    }

    public void searchActivityPaused() {
        searchActivityVisible = false;
    }

    public boolean searchActivityVisible() {
        return searchActivityVisible;
    }

    public void chatAcitivityResumed() {
        chatActivityVisible = true;
    }

    public void chatActivityPaused() {
        chatActivityVisible = false;
    }

    public boolean isChatActivityVisible() {
        return chatActivityVisible;
    }

    public void handshakeActivityResumed() {
        handshakeActivityVisible =  true;
    }

    public void handshaekActivityPaused() {
        handshakeActivityVisible = false;
    }

    public boolean isHandshakeActivityVisible() {
        return handshakeActivityVisible;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return mActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return mServiceInjector;
    }

    @Override
    protected void attachBaseContext(Context base) {
        Utils.Companion.invalidateCurrentLocale(base);
        super.attachBaseContext(base);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Utils.Companion.invalidateCurrentLocale(this);
    }
}
