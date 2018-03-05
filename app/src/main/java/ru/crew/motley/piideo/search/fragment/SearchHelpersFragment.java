package ru.crew.motley.piideo.search.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import org.parceler.Parcels;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.crew.motley.piideo.ButterFragment;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.network.neo.transaction.Data;
import ru.crew.motley.piideo.search.SearchListener;
import ru.crew.motley.piideo.search.SearchRepeaterSingleton;
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

    @BindView(R.id.searchRecycler)
    RecyclerView mSearchRecycler;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    private SearchAdapter mSearchAdapter;

    private Member mMember;
    private List<Member> mMembers = new LinkedList<>();

    public interface NoHelpersCallback {
        void showNoOneCanHelp();
    }


    public static SearchHelpersFragment newInstance(Parcelable member, SearchListener listener) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEMBER, member);
        SearchHelpersFragment fragment = new SearchHelpersFragment();
        fragment.setArguments(args);
        fragment.mCallback = listener;
        fragment.mHelpersCallback = (NoHelpersCallback) listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMember = Parcels.unwrap(getArguments().getParcelable(ARG_MEMBER));
        mSearchAdapter = new SearchAdapter(mMembers, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentLayout = R.layout.fragment_search_result;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mSearchRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSearchRecycler.setAdapter(mSearchAdapter);
        startSearch();
        return v;
    }

    private void startSearch() {
        String searchSubject = SharedPrefs.getSearchSubject(getActivity());
        Statement search = searchRequest(searchSubject);
        Statements statements = new Statements();
        statements.getValues().add(search);
        NeoApi api = NeoApiSingleton.getInstance();
        api.executeStatement(statements)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaction -> {
                            List<Data> responseData = transaction.getResults().get(0).getData();
                            if (responseData.isEmpty()) {
                                Toast.makeText(getActivity(), R.string.sch_no_result, Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }
                            mMembers.clear();
                            for (Data item : responseData) {
                                String targetFriendOfFriend = item.getRow()
                                        .get(0)
                                        .getValue();
                                Member member = Member.fromJson(targetFriendOfFriend);
                                Log.d(TAG, member.toString());
                                mMembers.add(member);
                            }
                            if (mMembers.isEmpty()) {
                                mHelpersCallback.showNoOneCanHelp();
                            } else {
                                mSearchAdapter.notifyDataSetChanged();
                            }
                        },
                        error -> {
                            Log.e(TAG, "Request target search problem", error);
                            Toast.makeText(getActivity(), R.string.ex_network, Toast.LENGTH_SHORT)
                                    .show();
                            if (!(error instanceof SocketTimeoutException)) {
                                throw new RuntimeException(error);
                            }
                        });
    }

    private Statement searchRequest(String searchSubject) {
        if (TextUtils.isEmpty(searchSubject)) {
            throw new IllegalStateException("Search mSubject can't be null or empty");
        }
        Statement subject = new Statement();
        subject.setStatement(Request.FIND_QUESTION_TARGET);
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
        Intent i = new Intent(getContext(), RequestService.class);
        SharedPrefs.setSearching(true, getContext());
        SharedPrefs.setSearchCount(mMembers.size(), getContext());
        SharedPrefs.progressTime(new Date().getTime(), getContext());
        getActivity().startService(i);
        mCallback.onNext();
    }


}
