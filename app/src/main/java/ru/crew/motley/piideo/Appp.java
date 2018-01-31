package ru.crew.motley.piideo;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import ru.crew.motley.piideo.di.DaggerMainComponent;


/**
 * Created by vas on 12/25/17.
 */


@ReportsCrashes(mailTo = "tabaqui.vn@gmail.com",
        customReportContent = {
/*                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,*/
                ReportField.LOGCAT
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.resToastText)
public class Appp extends Application implements HasActivityInjector, HasServiceInjector {

    @Inject
    DispatchingAndroidInjector<Activity> mActivityInjector;

    @Inject
    DispatchingAndroidInjector<Service> mServiceInjector;

    private boolean activityVisible = false;
    private boolean chatFragmentVisible = false;

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
//        ACRA.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(this);
//        ACRA.init(this);
    }

    public void activityResumed() {
        activityVisible = true;
    }

    public void activityPaused() {
        activityVisible = false;
    }

    public boolean isActivityVisible() {
        return activityVisible;
    }

    public void chatFragmentResumed() {
        chatFragmentVisible = true;
    }

    public void chatFragmentPaused() {
        chatFragmentVisible = false;
    }

    public boolean isChatVisible() {
        return chatFragmentVisible;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return mActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return mServiceInjector;
    }
}
