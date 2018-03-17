package ru.crew.motley.piideo.search.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.parceler.Parcels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.NetworkErrorCallback;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.network.neo.transaction.Data;
import ru.crew.motley.piideo.search.Country;
import ru.crew.motley.piideo.search.SearchListener;
import ru.crew.motley.piideo.search.SendRequestCallback;
import ru.crew.motley.piideo.search.adapter.SearchAdapter;
import ru.crew.motley.piideo.search.service.RequestService;

/**
 * Created by vas on 12/18/17.
 */

public class SearchHelpersFragment extends ButterFragment implements SendRequestCallback {

    private static final String TAG = SearchHelpersFragment.class.getSimpleName();
    private static final String ARG_MEMBER = "arg_member";


    private SearchListener mCallback;
    private NoHelpersCallback mHelpersCallback;
    private NetworkErrorCallback mErrorCallback;

    @BindView(R.id.searchRecycler)
    RecyclerView mSearchRecycler;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    private SearchAdapter mSearchAdapter;

    private Member mMember;
    private List<Member> mMembers = new LinkedList<>();
    private List<Country> mCountries = new ArrayList<>();

    public interface NoHelpersCallback {
        void showNoOneCanHelp();
    }


    public static SearchHelpersFragment newInstance(Parcelable member, SearchListener listener, NetworkErrorCallback errorCallback) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEMBER, member);
        SearchHelpersFragment fragment = new SearchHelpersFragment();
        fragment.setArguments(args);
        fragment.mCallback = listener;
        fragment.mHelpersCallback = (NoHelpersCallback) listener;
        fragment.mErrorCallback = errorCallback;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMember = Parcels.unwrap(getArguments().getParcelable(ARG_MEMBER));
        mSearchAdapter = new SearchAdapter(mMembers, mCountries, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_search_result;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mSearchRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSearchRecycler.setAdapter(mSearchAdapter);
        startSearch0();
        return v;
    }

    private Statement searchRequest(String searchSubject) {
        if (TextUtils.isEmpty(searchSubject)) {
            throw new IllegalStateException("Search mSubject can't be null or empty");
        }
        Statement subject = new Statement();
        subject.setStatement(Request.FIND_QUESTION_TARGET_1);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, mMember.getPhoneNumber());
        parameters.getProps().put(Request.Var.NAME, searchSubject);
        parameters.getProps().put(Request.Var.NAME_2, mMember.getSchool().getName());
        subject.setParameters(parameters);
        return subject;
    }


    @Override
    public void onClick(String receiverId, View view) {
        RequestDialog.getInstance(dialog -> onMessageInput(receiverId))
                .show(getChildFragmentManager(), "dialog");
    }

    private void onMessageInput(String receiverId) {
        Member target = null;
        for (Member member : mMembers) {
            if (receiverId.equals(member.getChatId())) {
                target = member;
            }
        }
        mMembers.remove(target);
        mMembers.add(0, target);
        ChatLab.get(getContext()).enqueue(mMembers);
//        RequestService.setMembers(mMembers);
//        RequestService.moveToFirstPosition(receiverId);
        Intent i = RequestService.getIntent(getContext());
        SharedPrefs.setSearching(true, getContext());
        SharedPrefs.setSearchCount(mMembers.size(), getContext());
        SharedPrefs.startSearchingTime(new Date().getTime(), getContext());
        getActivity().startService(i);
        Log.d(TAG, " on next ");
        mCallback.onNext();
    }

    private Single<List<Country>> loadCountries() {
        return Single.fromCallable(() -> {
            List<Country> data = new ArrayList<>(233);
            BufferedReader reader = null;
            try {
                InputStream countriesStream = getContext()
                        .getAssets()
                        .open("countries.dat");
                reader = new BufferedReader(new InputStreamReader(countriesStream, "UTF-8"));
                String line;
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    Country c = new Country(line, i);
                    data.add(c);
                    i++;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error countries file reading", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error countries reader closing", e);
                    }
                }
            }
            return data;
        }).subscribeOn(Schedulers.io());
    }

    private void startSearch0() {
        String searchSubject = SharedPrefs.getSearchSubject(getActivity());
        Statement search = searchRequest(searchSubject);
        Statements statements = new Statements();
        statements.getValues().add(search);
        NeoApi api = NeoApiSingleton.getInstance();
        Single<List<Member>> membersRequest = api.executeStatement(statements)
                .map(transaction -> {
                    List<Data> responseData = transaction.getResults().get(0).getData();
                    List<Member> members = new ArrayList<>();
                    for (Data item : responseData) {
                        String targetFriendOfFriend = item.getRow()
                                .get(0)
                                .getValue();
                        Member member = Member.fromJson(targetFriendOfFriend);
                        Log.d(TAG, member.toString());
                        members.add(member);
                    }
                    return members;
                });
        Single<List<Country>> countriesLoading = loadCountries();
        Single.zip(
                membersRequest/*.onErrorReturn(error -> {
                    mErrorCallback.onError();
                    return new ArrayList<>();
                })*/,
                countriesLoading,
                ((members, countries) -> {
                    mMembers.clear();
                    mMembers.addAll(members);
                    mCountries.clear();
                    mCountries.addAll(countries);
                    return 0;
                })).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(useless -> {
                            if (mMembers.isEmpty()) {
                                mHelpersCallback.showNoOneCanHelp();
                            } else {
                                mSearchAdapter.notifyDataSetChanged();
                            }
                        },
                        error -> {
                            mErrorCallback.onError();
                        });
    }


}
