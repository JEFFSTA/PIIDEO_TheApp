package ru.crew.motley.piideo.chat.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ru.crew.motley.piideo.Appp;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.chat.ChatAdapter;
import ru.crew.motley.piideo.chat.MessagesAdapter;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.chat.model.PiideoLoader;
import ru.crew.motley.piideo.fcm.FcmMessage;
import ru.crew.motley.piideo.fcm.MessagingService;
import ru.crew.motley.piideo.fcm.Receiver;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.piideo.activity.PhotoActivity;
import ru.crew.motley.piideo.splash.SplashActivity;
import ru.crew.motley.piideo.util.TimeUtils;

import static ru.crew.motley.piideo.piideo.service.Recorder.HOME_PATH;

/**
 * Created by vas on 12/22/17.
 */

public class ChatFragment extends ButterFragment
        implements ChatAdapter.PiideoCallback, MessagesAdapter.PiideoLoaderCallback {

    public interface PiideoShower {
        void showPiideo(String piideoFileName);
    }

    private static final String TAG = ChatFragment.class.getSimpleName();
    public static final long CHAT_TIMEOUT = 300L;
    private static final String ARG_DB_MESSAGE_ID = "local_db_id";

    @BindView(R.id.chat_recycler)
    RecyclerView mChatRecycler;
    @BindView(R.id.messageInput)
    EditText mMessageInput;
    @BindView(R.id.topic)
    TextView topicText;
    @BindView(R.id.watch)
    TextView watchText;

    @Inject
    PiideoLoader mPiideoLoader;

    private String mMessageId;
    private FcmMessage mFcmMessage;

    private DatabaseReference mDatabase;

    private PiideoShower mPiideoShower;

    private CompositeDisposable disposables = new CompositeDisposable();

    public static ChatFragment newInstance(String dbMessageId, PiideoShower piideoShower) {
        ChatFragment f = new ChatFragment();
        f.mPiideoShower = piideoShower;
        Bundle args = new Bundle();
        args.putString(ARG_DB_MESSAGE_ID, dbMessageId);
        f.setArguments(args);
        return f;
    }

    Handler handler = new Handler();
    Runnable mTimer;

    private class TimerDelay implements Runnable {

        private long seconds = 60;

        public TimerDelay(long seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            if (seconds < 0) {
                handler.removeCallbacks(mTimer);
                showRateView();
                makeMeFree();
            }
            if (watchText != null && seconds >= 0) {
                watchText.setText(timeMin(seconds) + ":" + timeSec(seconds));
            }
            seconds--;
            handler.postDelayed(this, 1000);
        }

        private String timeMin(long seconds) {
            long min = seconds / 60;
            if (min > 9) {
                return "" + min;
            }
            return "0" + min;
        }

        private String timeSec(long seconds) {
            long sec = seconds % 60;
            if (sec > 9) {
                return "" + sec;
            }
            return "0" + sec;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Appp) getActivity().getApplication()).chatAcitivityResumed();
        long chatStartTime = SharedPrefs.loadChatStartTime(getActivity());
        long chatTimeout = CHAT_TIMEOUT - (int) TimeUnit.MILLISECONDS.toSeconds(
                new Date().getTime() - chatStartTime);
        mTimer = new TimerDelay(chatTimeout);
        handler.post(mTimer);

        Log.d("PiideoLoader", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        ((Appp) getActivity().getApplication()).chatActivityPaused();
        handler.removeCallbacks(mTimer);
        Log.d("PiideoLoader", "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposables = new CompositeDisposable();
        mMessageId = getArguments().getString(ARG_DB_MESSAGE_ID);
        ChatLab lab = ChatLab.get(getActivity().getApplicationContext());
        mFcmMessage = lab.getReducedFcmMessage(mMessageId);
        long startChatTime = SharedPrefs.loadChatStartTime(getActivity());
        if (startChatTime == -1) {
            startChatTime = new Date().getTime();
            SharedPrefs.saveChatStartTime(startChatTime, getActivity());
            SharedPrefs.saveChatMessageId(mMessageId, getActivity());
            makeMeBusy(startChatTime);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_chat;
        View v;
        if (getView() != null) {
            v = getView();
        } else {
            v = super.onCreateView(inflater, container, savedInstanceState);
        }
//        mChatAdapter = new ChatAdapter(mPiideoRows, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        mChatRecycler.setLayoutManager(layoutManager);
        attachRecyclerAdapter();
//        v.setFocusableInTouchMode(true);
//        v.requestFocus();
//        v.setOnKeyListener((v1, keyCode, event) -> {
//            Log.i(TAG, "keyCode: " + keyCode);
//            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
//                Log.i(TAG, "onKey Back listener is working!!!");
//                removeChatMessages();
//            }
//            return false;
//        });
        return v;
    }

    @OnClick(R.id.piideo)
    public void makePiideo() {
        Parcelable message = Parcels.wrap(mFcmMessage);
        Intent i = PhotoActivity.getIntent(mMessageId, message, getActivity());
        startActivity(i);
    }

    private void cancelRejectTimeout() {
        if (mChatRecycler.getAdapter().getItemCount() == 2 &&
                mChatRecycler.getAdapter().getItemViewType(0) == MessagesAdapter.VIEW_TYPE_HELLO) {
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getActivity(), Receiver.class);
            intent.setAction("action");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
            alarmManager.cancel(pendingIntent);
        }
    }

    @OnClick(R.id.sendMessage)
    public void sendMessage() {
        // 1 because there's stub hello message
        sendAcknowledge();
        cancelRejectTimeout();
        long timestamp = TimeUtils.Companion.gmtTimeInMillis();
        long dayTimestamp = TimeUtils.Companion.gmtDayTimestamp(timestamp);
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp);
        String body = mMessageInput.getText().toString().trim();
        FcmMessage message = new FcmMessage(
                timestamp,
                -timestamp,
                dayTimestamp,
                mFcmMessage.getTo(),
                mFcmMessage.getFrom(),
                body,
                "message",
                mFcmMessage.getFrom() + "_" + mFcmMessage.getFrom());
        // 1 because there's stub hello message
        if (mChatRecycler.getAdapter().getItemCount() > 1 ||
                mChatRecycler.getAdapter().getItemViewType(0) != MessagesAdapter.VIEW_TYPE_HELLO) {
            mDatabase
                    .child("notifications")
                    .child("messages")
                    .push()
                    .setValue(message);
        }
        mDatabase
                .child("messages")
                .child(mFcmMessage.getFrom())
                .child(mFcmMessage.getTo())
                .push()
                .setValue(message);
        if (!mFcmMessage.getFrom().equals(mFcmMessage.getTo())) {
            mDatabase
                    .child("messages")
                    .child(mFcmMessage.getTo())
                    .child(mFcmMessage.getFrom())
                    .push()
                    .setValue(message);
        }
        mMessageInput.setText("");
    }


    private void attachRecyclerAdapter() {
        Query messagesQuery = mDatabase
                .child("messages")
                .child(mFcmMessage.getTo())
                .child(mFcmMessage.getFrom())
                .orderByChild("negatedTimestamp");
        FirebaseRecyclerOptions<FcmMessage> options =
                new FirebaseRecyclerOptions.Builder<FcmMessage>()
                        .setQuery(messagesQuery, FcmMessage.class)
                        .setLifecycleOwner(this)
                        .build();
        RecyclerView.Adapter adapter = new MessagesAdapter(mFcmMessage.getTo(), this, options);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mChatRecycler.smoothScrollToPosition(0);
            }
        });
        mChatRecycler.setAdapter(adapter);
    }

    @Override
    public void onClick(String piideoFileName) {
        mPiideoShower.showPiideo(piideoFileName);
    }

//    private void removeChatMessages() {
//        mDatabase
//                .child("messages")
//                .child(mFcmMessage.getTo())
//                .child(mFcmMessage.getFrom())
//                .removeValue();
//    }

    @Override
    public void send(FcmMessage message, ImageView piideoImage, View progress) {
        showPiideoImage(message.getContent(), piideoImage);
        piideoImage.setOnClickListener(v -> mPiideoShower.showPiideo(message.getContent()));
        mPiideoLoader.send0(
                message.getContent(),
                message.getFrom(),
                message.getTo())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next -> {
                            progress.setVisibility(View.GONE);
                        },
                        error -> {
                            Log.e("Piideo", "Send error", error);
                        });
    }

    private String fileImagePath(String name) throws IOException {
        String recName = name + ".jpg";
        return HOME_PATH + recName;
    }

    private void showPiideoImage(String piideoFileName, ImageView piideoImage) {
        try {
            String filePath = fileImagePath(piideoFileName);
            piideoImage.setOnClickListener(v -> {
//            mPiideoCallback.onClick(piideoFileName);
            });
            Picasso.with(piideoImage.getContext())
                    .load(new File(filePath))
                    .fit()
                    .centerCrop()
                    .into(piideoImage);
        } catch (IOException ex) {
            Log.e(TAG, "Read local piideo image error", ex);
        }
    }

    @Override
    public void receive(FcmMessage message, ImageView piideoImage, View progress) {
        disposables.add(
                mPiideoLoader.receive(message.getContent(), message.getFrom(), message.getTo())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(next -> {
                                    progress.setVisibility(View.GONE);
                                    showPiideoImage(message.getContent(), piideoImage);
                                    piideoImage.setOnClickListener(v -> mPiideoShower.showPiideo(message.getContent()));
                                },
                                error -> {
                                    Log.e("Piideo", "Receive error", error);
                                })
        );
    }

    private void sendAcknowledge() {
        if (mChatRecycler.getAdapter().getItemCount() == 1 &&
                mChatRecycler.getAdapter().getItemViewType(0) == MessagesAdapter.VIEW_TYPE_HELLO) {
            long now = TimeUtils.Companion.gmtTimeInMillis();
            FcmMessage message = new FcmMessage(
                    now,
                    -now,
                    TimeUtils.Companion.gmtDayTimestamp(now),
                    mFcmMessage.getTo(),
                    mFcmMessage.getFrom(),
                    "",
                    MessagingService.ACK,
                    mFcmMessage.getTo() + "_" + mFcmMessage.getFrom());
            mDatabase.child("notifications")
                    .child("handshake")
                    .push()
                    .setValue(message);
        }
    }

    private void showRateView() {
        // TODO: 002 02.02.18 implement rate view
        Intent i = SplashActivity.getIntent(getActivity());
        getActivity().startActivity(i);
        getActivity().finish();
        SharedPrefs.clearChatStartTime(getActivity());
        SharedPrefs.clearChatMessageId(getActivity());
    }

    private Statement createBusyRequest(long startChatTime) {
        ChatLab lab = ChatLab.get(getActivity());
        Member member = lab.getMember();
        Statement statement = new Statement();
        statement.setStatement(Request.MAKE_ME_BUSY);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, member.getPhoneNumber());
        parameters.getProps().put(Request.Var.DLG_TIME, startChatTime);
        statement.setParameters(parameters);
        return statement;
    }

    private void makeMeBusy(long startChatTime) {
        Statements statements = new Statements();
        statements.getValues().add(createBusyRequest(startChatTime));
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements).subscribe();
    }

    private Statement createFreeRequest() {
        ChatLab lab = ChatLab.get(getActivity());
        Member member = lab.getMember();
        Statement statement = new Statement();
        statement.setStatement(Request.MAKE_ME_BUSY);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, member.getPhoneNumber());
        statement.setParameters(parameters);
        return statement;
    }

    private void makeMeFree() {
        Statements statements = new Statements();
        statements.getValues().add(createFreeRequest());
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements).subscribe();
    }
}
