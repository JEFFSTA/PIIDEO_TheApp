package ru.crew.motley.piideo.search.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.parceler.Parcels;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.fcm.User;
import ru.crew.motley.piideo.handshake.activity.RequestListenerActivity;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.registration.activity.UserSetupActivity;
import ru.crew.motley.piideo.search.SearchListener;
import ru.crew.motley.piideo.search.fragment.SearchResultFragment;
import ru.crew.motley.piideo.search.fragment.SearchSubjectFragment;
import ru.crew.motley.piideo.search.fragment.UselessFragment;
import ru.crew.motley.piideo.splash.SplashActivity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SearchActivity extends RequestListenerActivity implements SearchListener {

    private static final String EXTRA_MEMBER = "member";

    private static final int SD_PERMISSIONS = 1001;

    private Member mMember;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

//    private ShowDialogReceiver mDialogReceiver;

    /**
     * Page order
     */
    @IntDef({Page.SUBJECT_PAGE, Page.BUTTON_PAGE, Page.SEARCH_PAGE, Page.COMPLETE})
    private @interface Page {
        int BUTTON_PAGE = 0;
        int SUBJECT_PAGE = 1;
        int SEARCH_PAGE = 2;
        int COMPLETE = 10;
    }

    @Page
    private int currentStep = Page.BUTTON_PAGE;

    public static Intent getIntent(Parcelable member, Context context) {
        Intent i = new Intent(context, SearchActivity.class);
        i.putExtra(EXTRA_MEMBER, member);
        return i;
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        ((Appp) getApplication()).activityResumed();
//        if (mDialogReceiver == null) {
//            mDialogReceiver = new ShowDialogReceiver(this);
//        }
//        IntentFilter filter = new IntentFilter(ShowDialogReceiver.Companion.getBROADCAST_ACTION());
//        registerReceiver(mDialogReceiver, filter);
//    }
//
//    @Override
//    protected void onPause() {
//        unregisterReceiver(mDialogReceiver);
//        ((Appp) getApplication()).activityPaused();
//        super.onPause();
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        initializeFirebaseAuthListener();
        Parcelable member = getIntent().getParcelableExtra(EXTRA_MEMBER);
        mMember = Parcels.unwrap(member);
//        Fragment fragment = SearchSubjectFragment.newInstance(member, this);
        int sdCardCheckWrite = ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE);
        int sdCardCheckRead = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);
        if (sdCardCheckWrite != PackageManager.PERMISSION_GRANTED || sdCardCheckRead != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},
                    SD_PERMISSIONS);
        } else {
            showNextStep();
        }
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
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                ChatLab lab = ChatLab.get(this);
                lab.deleteMember();
                Intent i = UserSetupActivity.getIntent(this);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
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
        Parcelable member = Parcels.wrap(mMember);
        nextStep();
//        Fragment fragment = SearchResultFragment.newInstance(member);
//        showFragment(fragment);
    }

    private void nextStep() {
        switch (currentStep) {
            case Page.BUTTON_PAGE:
                currentStep = Page.SUBJECT_PAGE;
                break;
            case Page.SUBJECT_PAGE:
                currentStep = Page.SEARCH_PAGE;
                break;
            case Page.SEARCH_PAGE:
                currentStep = Page.COMPLETE;
                break;
            case Page.COMPLETE:
                throw new IllegalStateException("Page you want to go out is final, just use onComplete.");
//                currentStep = Page.COMPLETE;
//                return;
            default:
                throw new IllegalStateException("Page you want to showChat is unsupported. Current step is " + currentStep);
        }
        showNextStep();
    }

    private void showNextStep() {
        Fragment fragment;
        switch (currentStep) {
            case Page.BUTTON_PAGE:
                fragment = UselessFragment.Companion.newInstance(this);
                break;
            case Page.SUBJECT_PAGE:
                Parcelable memberForVerification = Parcels.wrap(mMember);
                fragment = SearchSubjectFragment.newInstance(memberForVerification, this);
                break;
            case Page.SEARCH_PAGE:
                Parcelable memberForSchool = Parcels.wrap(mMember);
                fragment = SearchResultFragment.newInstance(memberForSchool);
                break;
            case Page.COMPLETE:
                // do nothing
//                if (!mMember.isRegistered()) {
//                    createNewMember();
//                } else {
//                    showSearch();
//                }
            default:
                throw new IllegalStateException("Page you want to showChat is unsupported");
        }
        showFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof SearchResultFragment) {
            currentStep = Page.SUBJECT_PAGE;
            showNextStep();
        } else {
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
        User user = new User(
//                firebaseUser.getDisplayName(),
//                firebaseUser.getEmail(),
                firebaseUser.getUid()
//                firebaseUser.getPhotoUrl() == null ? "" : firebaseUser.getPhotoUrl().toString()
        );

        mDatabase.child("users")
                .child(user.getUid()).setValue(user);

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
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case SD_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showNextStep();
                } else {
                    new Handler().postDelayed(() ->
                                    ActivityCompat.requestPermissions(
                                            this,
                                            new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},
                                            SD_PERMISSIONS),
                            1000);
                }
                break;
        }
    }
}
