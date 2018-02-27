package ru.crew.motley.piideo.search;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayDeque;
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
    private static final int REQUEST_DELAY = 50;

    private static volatile SearchRepeaterSingleton INSTANCE;

    public static SearchRepeaterSingleton instance(Context context) {
        SearchRepeaterSingleton local = INSTANCE;
        if (local == null) {
            synchronized (SearchRepeaterSingleton.class) {
                local = INSTANCE;
                if (local == null) {
                    Member member = ChatLab.get(context).getMember();
                    local = new SearchRepeaterSingleton(member);
                    INSTANCE = local;
                }
            }
        }
        return local;
    }

    public static SearchRepeaterSingleton newInstance(Context context) {
        Member member = ChatLab.get(context).getMember();
        SearchRepeaterSingleton local = new SearchRepeaterSingleton(member);
        INSTANCE = local;
        return local;
    }

    private Deque<Member> mMembers = new LinkedList<>();
    private Observable<Long> mSearchRepeater;
    private Observable<Single<Member>> mSearch;
    private PublishSubject<Long> mContinuousChain;
    private Disposable mSearchChainSubscription;
    private CompositeDisposable mRequestSubscriptions;
    private Disposable mSearchSubscription;
    private DatabaseReference mDatabase;
    private Member mMember;
    private boolean mOn;


    private SearchRepeaterSingleton() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private SearchRepeaterSingleton(Member member) {
        this();
        mMember = member;
//        mContinuousChain = BehaviorSubject.create();

        createSearchRepeater();
    }

    private void createSearchRepeater() {
        mRequestSubscriptions = new CompositeDisposable();
        mSearchRepeater = Observable.interval(0, REQUEST_DELAY, TimeUnit.SECONDS)
                .takeUntil(aLong -> {
                    Log.d(TAG, "222" + mMembers.isEmpty());
                    return mMembers.isEmpty();
                });
        mContinuousChain = PublishSubject.create();
        mSearchRepeater.subscribe(mContinuousChain);
        mSearch = mSearchRepeater.map(item -> {
            Member member1 = getNextMember();
            Log.d(TAG, "" + (member1 == null));
            return member1;
        })
                .map(item2 -> {
                    Single<Member> single = findReceiverFriendAndRequest(item2).subscribeOn(Schedulers.io());
                    return single;
                })
                .doOnNext(request -> {
                            request.subscribeOn(Schedulers.io()).subscribe(member1 -> {
                                        Log.e(TAG, "test");
                                    },
                                    error -> Log.e(TAG, "Error 1", error));
                            Single subscription = request.doOnSuccess(member1 -> {
                            }/* mMembers.poll()*/);
                            mRequestSubscriptions.add(subscription.subscribe());
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
    }

    private void startSearchChain() {
        stopSearchChain();
        mOn = true;
        createSearchRepeater();
        mSearchChainSubscription = mContinuousChain.subscribe(o -> {
                    Log.d(TAG, "Emmit");
                }, error -> {
                    Log.e(TAG, "Error 2", error);
                },
                () -> {
                    Log.d(TAG, "complete chain");
                    createSearchRepeater();
                    mMembers = new LinkedList<>();
                    mOn = false;
                });
        mSearchSubscription = mSearch.subscribe();
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
//        mContinuousChain.onComplete();
    }

    public boolean isOn() {
        return mOn;
    }

    private Member getNextMember() {
        return mMembers.poll();
    }

    private void sendRequest(Member receiver) {
        long timestamp = TimeUtils.Companion.gmtTimeInMillis();
        long dayTimestamp = TimeUtils.Companion.gmtDayTimestamp(timestamp);
        String ownerId = mMember.getChatId();
        String directRequestMarker = "";
        if (receiver.getReceivedFrom().getChatId().equals(ownerId)) {
            directRequestMarker = "++";
        }

        FcmMessage message =
                new FcmMessage(
                        timestamp,
                        -timestamp,
                        dayTimestamp,
                        ownerId,
                        receiver.getChatId(),
                        directRequestMarker + receiver.getReceivedFrom().getPhoneNumber(),
                        MessagingService.SYN,
                        ownerId + "_" + receiver.getChatId(),
                        false);
        mDatabase
                .child("notifications")
                .child("handshake")
                .push()
                .setValue(message);
    }

    public void startSearch() {
        startSearchChain();
    }

    public void stopSearch() {
        stopSearchChain();
    }

    public void next() {
        stopSearchChain();
        startSearchChain();
    }

    public Observable<Long> repeatableSearchObservable() {
        Log.d(TAG, mContinuousChain.toString());
        return mContinuousChain;
    }

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

}
