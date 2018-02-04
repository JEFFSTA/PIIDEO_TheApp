package ru.crew.motley.piideo.search.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.network.Member;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<Member> mMembers;
    private SendRequestCallback mListener;

    public SearchAdapter(List<Member> members, SendRequestCallback listener) {
        mMembers = members;
        mListener = listener;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search, parent, false);
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        Member member = mMembers.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return mMembers.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {

        TextView mPhoneNumber;

        public SearchViewHolder(View itemView) {
            super(itemView);
            mPhoneNumber = itemView.findViewById(R.id.phone_number);
        }

        public void bind(Member member) {
            mPhoneNumber.setText(member.getPhoneNumber());
            String receiverId = member.getChatId();
            itemView.setOnClickListener(v -> {
                startChat(receiverId);
            });
        }

        private void startChat(String receiverId) {
            mListener.onClick(receiverId, itemView);
        }
    }

}
