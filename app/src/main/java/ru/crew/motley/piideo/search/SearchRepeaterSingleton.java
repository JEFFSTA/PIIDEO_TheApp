package ru.crew.motley.piideo.search;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.fcm.FcmMessage;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.network.neo.transaction.Data;
import ru.crew.motley.piideo.util.TimeUtils;

import static android.content.ContentValues.TAG;

/**
 * Created by vas on 1/21/18.
 */

public class SearchRepeaterSingleton {

    private static volatile SearchRepeaterSingleton INSTANCE;

    public static SearchRepeaterSingleton instance(/*Member member, */Context context) {
        SearchRepeaterSingleton local = INSTANCE;
        if (local == null) {
            synchronized (SearchRepeaterSingleton.class) {
                local = INSTANCE;
                if (local == null) {
                    Member member = ChatLab.get(context).getMember();
                    local = new SearchRepeaterSingleton(member, context);
                    INSTANCE = local;
                }
            }
        }
        return local;
    }

    public static SearchRepeaterSingleton newInstance(Context context) {
        Member member = ChatLab.get(context).getMember();
        SearchRepeaterSingleton local = new SearchRepeaterSingleton(member, context);
        INSTANCE = local;
        return local;
    }

//    private static final int REQUEST_DELAY = 20;

    private Deque<Member> mMembers = new LinkedList<>();
//    private Disposable mSearchRepeater;
    private DatabaseReference mDatabase;
    private Member mMember;
//    private WeakReference<Context> mWeakReference;
    private BehaviorSubject<Member> mMemberSubject;

    private SearchRepeaterSingleton() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private SearchRepeaterSingleton(Member member, Context context) {
        this();
        mMember = member;
//        mWeakReference = new WeakReference(context);
        mMemberSubject = BehaviorSubject.create();
    }

//    public void setContext(Context context) {
//        mWeakReference = new WeakReference<>(context);
//    }

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

//    private void startSearchChain() {
//        stopSearchChain();
//        mSearchRepeater =
//                Observable.interval(0, REQUEST_DELAY, TimeUnit.SECONDS)
//                        .map(item -> getNextMember())
//                        .subscribe(member -> sendRequest(member.getChatId()),
//                                error -> {
//                                    Log.e(TAG, "Search Chain Exception", error);
//                                });
//    }

//    private void stopSearchChain() {
//        if (mSearchRepeater != null && !mSearchRepeater.isDisposed()) {
//            mSearchRepeater.dispose();
//            mSearchRepeater = null;
//        }
//    }

    private Member getNextMember() {
        return mMembers.poll();
    }

    private void sendRequest(String receiverId) {
        long timestamp = TimeUtils.Companion.gmtTimeInMillis();
        long dayTimestamp = TimeUtils.Companion.gmtDayTimestamp(timestamp);
        String ownerId = mMember.getChatId();

        FcmMessage message =
                new FcmMessage(
                        timestamp,
                        -timestamp,
                        dayTimestamp,
                        ownerId,
                        receiverId,
                        "",
                        "synchronize",
                        ownerId + "_" + receiverId);
        mDatabase
                .child("notifications")
                .child("handshake")
                .push()
                .setValue(message);
    }

//    public void startSearch() {
//        Context context = mWeakReference.get();
//        if (context == null) {
//            return;
//        }
//        String searchSubject = SharedPrefs.getSearchSubject(context);
//        Statement search = searchRequest(searchSubject);
//        Statements statements = new Statements();
//        statements.getValues().add(search);
//        NeoApi api = NeoApiSingleton.getInstance();
//        api.executeStatement(statements)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(transaction -> {
//                            List<Data> responseData = transaction.getResults().get(0).getData();
//                            if (responseData.isEmpty()) {
//                                Context context1 = mWeakReference.get();
//                                if (context1 != null) {
//                                    Toast.makeText(context, R.string.sch_no_result, Toast.LENGTH_SHORT)
//                                            .show();
//                                }
//                                return;
//                            }
//                            for (Data item : responseData) {
//                                String response = item
//                                        .getRow()
//                                        .get(0)
//                                        .getValue();
//                                Member member = Member.fromJson(response);
//                                Log.d(TAG, member.toString());
//                                mMembers.add(member);
//                            }
//                            startSearchChain();
//                        },
//                        error -> {
//                            Log.e(TAG, "Error search chain execution", error);
//                            Context context1 = mWeakReference.get();
//                            if (context1 != null) {
//                                Toast.makeText(context, R.string.sch_no_result, Toast.LENGTH_SHORT)
//                                        .show();
//                            }
//                            stopSearch();
//                            if (!(error instanceof SocketTimeoutException)) {
//                                throw new RuntimeException(error);
//                            }
//                        });
//    }

//    public void stopSearch() {
//        stopSearchChain();
//    }

    public void next() {
//        stopSearchChain();
        if (mMembers.isEmpty()) {
            mMemberSubject.onComplete();
            return;
        }
        mMemberSubject.onNext(getNextMember());
    }

    public Observable<Member> searchObservable() {
        return mMemberSubject.map(item -> {
            sendRequest(item.getChatId());
            return item;
        }).subscribeOn(Schedulers.io());
    }

//    private Statement searchRequest(String searchSubject) {
//        if (TextUtils.isEmpty(searchSubject)) {
//            throw new IllegalStateException("Search mSubject can't be null or empty");
//        }
//        Statement subject = new Statement();
//        subject.setStatement(Request.FIND_QUESTION_TARGET);
//        Parameters parameters = new Parameters();
//        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
//        parameters.getProps().put(Request.Var.NAME, searchSubject);
//        parameters.getProps().put(Request.Var.NAME_2, mMember.getSchool().getName());
//        subject.setParameters(parameters);
//        return subject;
//    }

}
