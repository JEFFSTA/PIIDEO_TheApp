package ru.crew.motley.piideo.search.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import io.reactivex.schedulers.Schedulers;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.fcm.FcmMessage;
import ru.crew.motley.piideo.fcm.MessagingService;
import ru.crew.motley.piideo.handshake.activity.HandshakeActivity;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.network.neo.transaction.Data;
import ru.crew.motley.piideo.search.Events;
import ru.crew.motley.piideo.search.fragment.SearchSubjectFragment;
import ru.crew.motley.piideo.search.fragment.SendingRequestFragment;
import ru.crew.motley.piideo.search.receiver.RequestReceiver;
import ru.crew.motley.piideo.util.TimeUtils;

/**
 * Created by vas on 3/3/18.
 */

public class RequestService extends IntentService {

    private static String TAG = RequestService.class.getSimpleName();

    public static int SERVICE_ID = 100501;

    //    private static volatile LinkedBlockingDeque<Member> mMembers = new LinkedBlockingDeque<>();
//    private static volatile int check;
//    private static volatile int mCount;
    private DatabaseReference mDatabase;
    private Member mMember;
    //    private static volatile boolean mIsOn;
    private static volatile SendingRequestFragment mFragment;

    private static volatile Object lock = new Object();

//    public static void setMembers(List<Member> members) {
//        synchronized (lock) {
//            Log.d(TAG, "lock sM" + lock.toString());
//            check = 123;
//            mMembers.clear();
//            for (Member member : members) {
//                mMembers.add(member);
//            }
//            mCount = mMembers.size();
//        }
//    }

//    public static int count() {
//        return mCount;
//    }

    public RequestService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ChatLab lab = ChatLab.get(this);
        mMember = lab.getMember();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "HANDLE " + Thread.currentThread().getName() +
                " " + Thread.currentThread().toString() +
                " " + Thread.currentThread().getId());

        doTheJob();
    }

    private void doTheJob() {
        Member next = ChatLab.get(this).pollNext();

        Log.d(TAG, " DO THE JOB " + Thread.currentThread().toString());
        Log.d(TAG, " DO THE JOB " + Thread.currentThread().getContextClassLoader().toString());
        if (next == null) {
            SharedPrefs.setSearching(false, this);
            SharedPrefs.setSearchCount(-1, this);
            if (mFragment != null) {
                mFragment.complete();
            } else {
                sendCompleteNotification();
            }
            return;
        }
        restartService0();
        findReceiverFriendAndRequest(next);
    }

    private void sendCompleteNotification() {
        Intent broadcast = new Intent();
        broadcast.setAction(Events.BROADCAST_NO_HELP);
        this.sendOrderedBroadcast(broadcast, null);
    }

    private void findReceiverFriendAndRequest(Member receiver) {
        Statements statements = new Statements();
        Statement statement = receiverFriendRequest(receiver);
        statements.getValues().add(statement);
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .subscribeOn(Schedulers.io())
                .map(transaction -> {
                    List<Data> responseData = transaction.getResults().get(0).getData();
                    String receiverFriendJson = responseData.get(0)
                            .getRow()
                            .get(0)
                            .getValue();
                    return Member.fromJson(receiverFriendJson);
                })
                .map(rf -> {
                    receiver.setReceivedFrom(rf);
                    return receiver;
                }).observeOn(Schedulers.io())
                .subscribe(this::sendRequest);
    }

    private void sendRequest(Member receiver) {
        long timestamp = TimeUtils.Companion.gmtTimeInMillis();
        long dayTimestamp = TimeUtils.Companion.gmtDayTimestamp(timestamp);
        String ownerId = mMember.getChatId();
        String directRequestMarker = "";
        if (receiver.getReceivedFrom().getChatId().equals(ownerId)) {
            directRequestMarker = "++";
        }

        String subject = "||" + SharedPrefs.getSearchSubject(this);
        String explanation = "|" + SharedPrefs.getRequestMessage(this);

        FcmMessage message =
                new FcmMessage(
                        timestamp,
                        -timestamp,
                        dayTimestamp,
                        ownerId,
                        receiver.getChatId(),
                        directRequestMarker + receiver.getReceivedFrom().getPhoneNumber() + subject + explanation,
                        MessagingService.SYN,
                        ownerId + "_" + receiver.getChatId(),
                        false);
        mDatabase
                .child("notifications")
                .child("handshake")
                .push()
                .setValue(message);
    }

    private Statement receiverFriendRequest(Member receiver) {
        Statement request = new Statement();
        request.setStatement(Request.FIND_TARGET_FRIEND);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
        parameters.getProps().put(Request.Var.F_PHONE, receiver.getPhoneNumber());
        request.setParameters(parameters);
        return request;
    }

    private void restartService() {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);


        Intent serviceIntent = new Intent(this, RequestService.class);
        serviceIntent.setAction("ru.crew.motley.piideo.BROADCAST_REQUEST");
        PendingIntent servicePendingIntent =
                PendingIntent.getService(this,
                        RequestService.SERVICE_ID,
                        serviceIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        long executeAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(50);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    executeAt,
                    servicePendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    executeAt,
                    servicePendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    executeAt,
                    servicePendingIntent);
        }
    }

    private void restartService0() {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, RequestReceiver.class);
        intent.setAction(Events.BROADCAST_REPEAT);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 33, intent, 0);
        long executeAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(50);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    executeAt,
                    pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    executeAt,
                    pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    executeAt,
                    pendingIntent);
        }
    }

    public static void stopRestarting(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent serviceIntent = new Intent(context, RequestService.class);
        serviceIntent.setAction("ru.crew.motley.piideo.BROADCAST_REPEAT");
        PendingIntent servicePendingIntent =
                PendingIntent.getService(context,
                        RequestService.SERVICE_ID,
                        serviceIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.cancel(servicePendingIntent);

    }

    public static void stopRestarting0(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent serviceIntent = new Intent(context, RequestService.class);
        serviceIntent.setAction("ru.crew.motley.piideo.BROADCAST_REPEAT");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 33, serviceIntent, 0);

        alarmManager.cancel(pendingIntent);

    }

    public synchronized static void fragmentCallback(SendingRequestFragment fragment) {
        mFragment = fragment;
    }
}
