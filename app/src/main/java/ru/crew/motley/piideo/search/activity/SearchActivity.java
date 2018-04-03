package ru.crew.motley.piideo.search.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.fcm.User;
import ru.crew.motley.piideo.handshake.activity.RequestListenerActivity;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.registration.activity.UserSetupActivity;
import ru.crew.motley.piideo.search.Events;
import ru.crew.motley.piideo.search.SearchListener;
import ru.crew.motley.piideo.search.fragment.NoCanHelpFragment;
import ru.crew.motley.piideo.search.fragment.NoCanHelpFragment2;
import ru.crew.motley.piideo.search.fragment.NoHelpFragment;
import ru.crew.motley.piideo.search.fragment.SearchHelpersFragment;
import ru.crew.motley.piideo.search.fragment.SearchSubjectFragment;
import ru.crew.motley.piideo.search.fragment.SendingRequestFragment;
import ru.crew.motley.piideo.search.fragment.UselessFragment;
import ru.crew.motley.piideo.settings.activity.SettingsActivity;
import ru.crew.motley.piideo.splash.SplashActivity;

import static ru.crew.motley.piideo.piideo.service.Recorder.HOME_PATH;

public class SearchActivity extends RequestListenerActivity
        implements SearchListener,
        SearchHelpersFragment.NoHelpersCallback {

    private static final String TAG = SearchActivity.class.getSimpleName();

    private static final String EXTRA_MEMBER = "member";

    private Member mMember;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    /**
     * Page order
     */
    @IntDef({Page.SUBJECT, Page.BUTTON, Page.HELPERS, Page.REQUEST, Page.REJECT, Page.NO_HELPERS, Page.COMPLETE})
    private @interface Page {
        int BUTTON = 0;
        int SUBJECT = 1;
        int HELPERS = 2;
        int REQUEST = 3;
        int REJECT = 4;
        int NO_HELPERS = 5;
        int COMPLETE = 10;
    }

    @Page
    private int currentStep = Page.BUTTON;

    public static Intent getIntent(Parcelable member, Context context) {
        Intent i = new Intent(context, SearchActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(EXTRA_MEMBER, member);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "createActivity " + currentStep);
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        initializeFirebaseAuthListener();
        Parcelable member = getIntent().getParcelableExtra(EXTRA_MEMBER);
        mMember = Parcels.unwrap(member);
        showNextStep();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mneu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.chat:
//                // TODO: 1/20/18 remove null from get intent
//                Intent chatIntent = ChatActivity.getIntent(null, this);
//                startActivity(chatIntent);
//                break;
            case R.id.settings:
                Intent ii = SettingsActivity.getIntent(this);
                startActivity(ii);
                break;
            case R.id.privacy_policy:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JEFFSTA/PIIDEO_Privacy-_Policy/wiki/Privacy-policy"));
                        startActivity(browserIntent);
//            case R.id.logout:
//                FirebaseAuth.getInstance().signOut();
//                ChatLab lab = ChatLab.get(this);
//                lab.deleteMember();
//                Intent i = UserSetupActivity.getIntent(this);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                finish();
//                break;
//
//            case R.id.dump:
//                File logFile = generateLog();
//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tabaqui.vn@gmail.com"});
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Log file");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Uri logUri = FileProvider.getUriForFile(
//                        this,
//                        /*getApplicationContext().getPackageName() + */"ru.crew.motley.piideo.fileprovider",
//                        logFile);
//                intent.putExtra(Intent.EXTRA_STREAM, logUri);
//                intent.setType("multipart/");
//                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private File generateLog() {
        File logFolder = new File(HOME_PATH, "log");
        if (!logFolder.exists()) {
            logFolder.mkdir();
        }
        String filename = "myapp_log_" + new Date().getTime() + ".log";

        File logFile = new File(logFolder, filename);
//        if (logFile.exists()) {
//            logFile.delete();
//        }
        try {
            String[] cmd = new String[]{"logcat", "-f", logFile.getAbsolutePath(), "-v", "time", "*:D"};
            Runtime.getRuntime().exec(cmd);
            Toast.makeText(this, "Log generated to: " + filename, Toast.LENGTH_SHORT);
            return logFile;
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        return null;
    }


    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onNext() {
        Parcelable member = Parcels.wrap(mMember);
        Log.d(TAG, "next step");
        nextStep();
//        Fragment fragment = SearchResultFragment.newInstance(member);
//        showFragment(fragment);
    }

    private void nextStep() {
        switch (currentStep) {
            case Page.BUTTON:
                currentStep = Page.SUBJECT;
                break;
            case Page.SUBJECT:
                currentStep = Page.HELPERS;
                break;
            case Page.HELPERS:
                currentStep = Page.REQUEST;
                break;
            case Page.REQUEST:
                currentStep = Page.REJECT;
                break;
            case Page.REJECT:
                currentStep = Page.COMPLETE;
                break;
            case Page.NO_HELPERS:
                break;
            case Page.COMPLETE:
                throw new IllegalStateException("Page you want to go out is final, just use onComplete.");
            default:
                throw new IllegalStateException("Page you want to showChat is unsupported. Current step is " + currentStep);
        }
        showNextStep();
    }

    private void showNextStep() {
        Fragment fragment;
        switch (currentStep) {
            case Page.BUTTON:
                fragment = UselessFragment.Companion.newInstance(this, this);
                break;
            case Page.SUBJECT:
                Parcelable memberForVerification = Parcels.wrap(mMember);
                fragment = SearchSubjectFragment.newInstance(memberForVerification, this, this);
                break;
            case Page.HELPERS:
                Parcelable memberForSchool = Parcels.wrap(mMember);
                fragment = SearchHelpersFragment.newInstance(memberForSchool, this, this);
                break;
            case Page.REQUEST:
                fragment = SendingRequestFragment.newInstance(this);
                break;
            case Page.REJECT:
                fragment = NoHelpFragment.newInstance(this);
                break;
//            case Page.NO_HELPERS:
//                fragment = NoCanHelpFragment2.newInstance();
//                break;
            case Page.COMPLETE:
                // do nothing
                return;
            default:
                throw new IllegalStateException("Page you want to showChat is unsupported");
        }
        showFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        if (errorShown()) {
            backFromError();
            return;
        }
        switch (currentStep) {
            case Page.HELPERS:
                currentStep = Page.SUBJECT;
                showNextStep();
                break;
            case Page.BUTTON:
            case Page.REQUEST:
                super.onBackPressed();
                break;
            case Page.REJECT:
                currentStep = Page.BUTTON;
                showNextStep();
                break;
            case Page.NO_HELPERS:
                currentStep = Page.SUBJECT;
                showNextStep();
                break;
            case Page.SUBJECT:
                super.onBackPressed();
        }
    }

    @Override
    public void onBack() {

    }

    private void initializeFirebaseAuthListener() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                addUserToDatabase(user);
                Log.d("@@@@", "home:signed_in:" + user.getUid());
            } else {
                Log.d("@@@@", "home:signed_out");
                Intent login = SplashActivity.getIntent(SearchActivity.this);
                startActivity(login);
                finish();
            }
        };
    }

    private void addUserToDatabase(FirebaseUser firebaseUser) {
        User user = new User(firebaseUser.getUid());
        mDatabase.child("users")
                .child(user.getUid())
                .setValue(user);

        String instanceId = FirebaseInstanceId.getInstance().getToken();
        if (instanceId != null) {
            mDatabase.child("users")
                    .child(firebaseUser.getUid())
                    .child("instanceId")
                    .setValue(instanceId);
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "start Activity " + currentStep);
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "stop Activity " + currentStep);
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private BroadcastReceiver mRejectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Abort notification");
            abortBroadcast();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "resume Activity " + currentStep);
        Log.d(TAG, "Activity  string " + this.toString());
        IntentFilter filter = new IntentFilter(Events.BROADCAST_NO_HELP);
        filter.setPriority(1);
        registerReceiver(mRejectReceiver, filter);
        if (!SharedPrefs.searchCompleted(this)) {
            currentStep = Page.REQUEST;
            showNextStep();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "pause Activity " + currentStep);
        unregisterReceiver(mRejectReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destroy Activity " + currentStep);
    }

    @Override
    public void showNoOneCanHelp() {
        currentStep = Page.NO_HELPERS;
        Fragment fragment = NoCanHelpFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

}
