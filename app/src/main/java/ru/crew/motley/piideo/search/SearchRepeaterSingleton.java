package ru.crew.motley.piideo.search;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.fcm.FcmMessage;
import ru.crew.motley.piideo.fcm.MessagingService;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.network.neo.transaction.Data;
import ru.crew.motley.piideo.util.TimeUtils;

/**
 * Created by vas on 1/21/18.
 */

public class SearchRepeaterSingleton {

    private static final String TAG = SearchRepeaterSingleton.class.getSimpleName();
    private static final int REQUEST_DELAY = 20;

    private static volatile SearchRepeaterSingleton INSTANCE;

    public static SearchRepeaterSingleton instance(Context context) {
        SearchRepeaterSingleton local = INSTANCE;
        if (local == null) {
            synchronized (SearchRepeaterSingleton.class) {
                local = INSTANCE;
                if (local == null) {
                    Member member = ChatLab.get(context).getMember();
                    local = new SearchRepeaterSingleton(member);
                    local.mContext = context.getApplicationContext();
                    INSTANCE = local;
                }
            }
        }
        Log.d(TAG, local.toString());
        return local;
    }

    public static SearchRepeaterSingleton newInstance(Context context) {
        Member member = ChatLab.get(context).getMember();
        SearchRepeaterSingleton local = new SearchRepeaterSingleton(member);
        local.mContext = context.getApplicationContext();
        INSTANCE = local;
        return local;
    }

    private Context mContext;

    private Deque<Member> mMembers = new LinkedList<>();
    private Observable<Long> mSearchRepeater;
    private Observable<Single<Member>> mSearch;
    private BehaviorSubject<Long> mContinuousChain;
    private Disposable mSearchChainSubscription;
    private CompositeDisposable mRequestSubscriptions;
    private Disposable mSearchSubscription;
    private DatabaseReference mDatabase;
    private Member mMember;
    private int membersAtStart;
    private boolean mOn;

    public int count() {
        return membersAtStart;
    }


    private SearchRepeaterSingleton() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private SearchRepeaterSingleton(Member member) {
        this();
        mMember = member;
        mTimer = Observable.interval(0, REQUEST_DELAY, TimeUnit.SECONDS);
        mRepeaterSubject = PublishSubject.create();
        Log.d(TAG, "Calling construct");
//        mContinuousChain = BehaviorSubject.create();

//        createSearchRepeater();
    }

    private void createSearchRepeater() {
        mRequestSubscriptions = new CompositeDisposable();
        mSearchRepeater = Observable.interval(0, REQUEST_DELAY, TimeUnit.SECONDS)
                .takeUntil(aLong -> {
                    Log.d(TAG, "222" + mMembers.isEmpty());
                    return mMembers.isEmpty();
                });
        mContinuousChain = BehaviorSubject.create();
        mSearchRepeater.subscribe(mContinuousChain);
        mSearch = mSearchRepeater.takeUntil(l -> {
            Log.d(TAG, "members size" + mMembers.size());
            return mMembers.isEmpty();
        })
                .map(item -> {
                    Member member1 = getNextMember();
                    Log.d(TAG, "map next member" + (member1 == null));
                    return member1;
                })
                .map(item2 -> {
                    Single<Member> single = findReceiverFriendAndRequest(item2).subscribeOn(Schedulers.io());
                    Log.d(TAG, "map single" + (single == null));
                    return single;
                })
                .doOnNext(request -> {
                            request.subscribeOn(Schedulers.io()).subscribe(member1 -> {
                                        Log.e(TAG, "test");
                                    },
                                    error -> Log.e(TAG, "Error 1", error));
                            Single<Member> subscription = request.doOnSuccess(member1 -> mMembers.poll());
                            mRequestSubscriptions.add(subscription.subscribe(
                                    m -> {
                                    },
                                    e -> {
                                        Log.e(TAG, "Request send error", e);
                                    }
                            ));
                        }
                );

    }

    public void moveToFirstPosition(String receiverId) {
        Member target = null;
        for (Member member : mMembers) {
            if (receiverId.equals(member.getChatId())) {
                target = member;
            }
        }
        mMembers.remove(target);
        mMembers.addFirst(target);
    }

    public void setMembers(List<Member> members) {
        mMembers = new LinkedList<>(members);
        membersAtStart = mMembers.size();
    }

    private void startSearchChain() {
        stopSearchChain();
        mOn = true;
        createSearchRepeater();
//        mSearchChainSubscription = mContinuousChain.subscribe(o -> {
//                    Log.d(TAG, "Emmit");
//                }, error -> {
//                    Log.e(TAG, "Error 2", error);
//                },
//                () -> {
//                    Log.d(TAG, "complete chain");
//                    createSearchRepeater();
//                    mMembers = new LinkedList<>();
//                    mOn = false;
//                });
        mSearchSubscription = mSearch.subscribe(memberSingle -> {
                    Log.d(TAG, "mmember single");
                },
                error -> {
                    Log.e(TAG, "mSS error", error);
                });
    }

    private void stopSearchChain() {
        mOn = false;
        mContinuousChain.onComplete();
        if (mSearchChainSubscription != null && !mSearchChainSubscription.isDisposed()) {
            mSearchChainSubscription.dispose();
        }
        if (mSearchSubscription != null && !mSearchSubscription.isDisposed()) {
            mSearchSubscription.dispose();
        }
        if (!mRequestSubscriptions.isDisposed()) {
            mRequestSubscriptions.dispose();
        }

    }

    public boolean isOn() {
        return mOn;
    }

    private Member getNextMember() {
        Log.d(TAG, "get member");
        return mMembers.peek();
    }

    private void sendRequest(Member receiver) {
        long timestamp = TimeUtils.Companion.gmtTimeInMillis();
        long dayTimestamp = TimeUtils.Companion.gmtDayTimestamp(timestamp);
        String ownerId = mMember.getChatId();
        String directRequestMarker = "";
        if (receiver.getReceivedFrom().getChatId().equals(ownerId)) {
            directRequestMarker = "++";
        }

        String subject = "||" + SharedPrefs.getSearchSubject(mContext);
        String explanation = "|" + SharedPrefs.getRequestMessage(mContext);

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

//    public void startSearch() {
//        startSearchChain();
//    }

//    public void stopSearch() {
//        stopSearchChain();
//    }

//    public void next() {
//        stopSearchChain();
//        startSearchChain();
//    }

//    public Observable<Long> repeatableSearchObservable() {
//        Log.d(TAG, mContinuousChain.toString());
//        return mContinuousChain;
//    }

    private Single<Member> findReceiverFriendAndRequest(Member receiver) {
        Statements statements = new Statements();
        Statement statement = receiverFriendRequest(receiver);
        statements.getValues().add(statement);
        NeoApi api = NeoApiSingleton.getInstance();

        return api.executeStatement(statements)
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
                })
                .map(r -> {
                    sendRequest(r);
                    return r;
                });
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


    private PublishSubject<Long> mRepeaterSubject;
    private Observable<Long> mTimer;
    private Disposable mTimerSubs;
    private CompositeDisposable mRequestSubss;

    public Observable<Long> subject() {
        return mRepeaterSubject;
    }

//    public void skip() {
//        if (mRequestSubss != null) {
//            mRequestSubss.dispose();
//        }
//        mRequestSubss = new CompositeDisposable();
//        if (mTimerSubs != null) {
//            mTimerSubs.dispose();
//        }
//        mTimerSubs = newTask();
//    }

//    public void start() {
//        mOn = true;
////        mRepeaterSubject = BehaviorSubject.create();
//        Log.d(TAG, "MTIMER " + Thread.currentThread().getName());
//        mRequestSubss = new CompositeDisposable();
//        mTimerSubs = newTask();
//    }

//    public void stop() {
//        mOn = false;
//        if (mTimerSubs != null) {
//            mTimerSubs.dispose();
//        }
//        if (mRequestSubss != null) {
//            mRequestSubss.dispose();
//        }
//    }

    private Disposable newTask() {
        Log.d(TAG, "new TASK " + Thread.currentThread().getName());
        return mTimer
//                .takeWhile(
//                l -> {
//                    Log.d(TAG, "" + mMembers.isEmpty());
//                    return !mMembers.isEmpty();
//                })
//                .map(l -> {
//                    Log.d(TAG, "poll");
//                    return mMembers.poll();
//                })
//                .map(m -> {
//                    Single<Member> single = findReceiverFriendAndRequest(m);
//                    Log.d(TAG, "map single " + (single == null));
//                    return single;
//                })
//                .map(s -> {
//                    mRequestSubss.add(s.subscribe());
//                    return 0L;
//                })

                .subscribe(
                        l -> {
                            mRepeaterSubject.onNext(l);
                            Log.d(TAG, "onNext");
                        },
                        e -> Log.e(TAG, "timer Task error", e),
                        () -> {
                            Log.d(TAG, "timer Task complete" + Thread.currentThread());
                            if (mMembers.isEmpty()) {
                                mRepeaterSubject.onComplete();
                            }
//                            stop();
                            // next string and its code smell like shit
                            mOn = true;
                            sendCompleteNotification();
                        });
    }

    private void sendCompleteNotification() {
        Intent broadcast = new Intent();
        broadcast.setAction(Events.BROADCAST_NO_HELP);
        mContext.sendOrderedBroadcast(broadcast, null);
    }


//    public void setAlarm() {
//        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(mContext, RequestService.class);
//        intent.setAction("ru.crew.motley.piideo.BROADCAST_REQUEST");
////        intent.putExtra("DB_MESSAGE_ID", dbMessageId)
//
//
//        Intent serviceIntent = new Intent(mContext, RequestService.class);
//// make sure you **don't** use *PendingIntent.getBroadcast*, it wouldn't work
//        PendingIntent servicePendingIntent =
//                PendingIntent.getService(mContext,
//                        RequestService.SERVICE_ID, // integer constant used to identify the service
//                        serviceIntent,
//                        PendingIntent.FLAG_CANCEL_CURRENT);  // FLAG to avoid creating a second service if there's already one running
//
//
////        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 99, intent, 0);
//        long executeAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(50);
////
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(
//                    AlarmManager.RTC_WAKEUP,
//                    executeAt,
//                    servicePendingIntent);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
//                    executeAt,
//                    servicePendingIntent);
//        } else {
//            alarmManager.set(AlarmManager.RTC_WAKEUP,
//                    executeAt,
//                    servicePendingIntent);
//        }
//    }
}
