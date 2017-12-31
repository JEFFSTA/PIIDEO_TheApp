package ru.crew.motley.piideo.search.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.crew.motley.piideo.R;

/**
 * Created by vas on 12/31/17.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<String> mPhoneNumbers;

    public SearchAdapter(List<String> phoneNumbers) {
        mPhoneNumbers = phoneNumbers;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search, parent, false);
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        String phoneNumber = mPhoneNumbers.get(position);
        holder.bind(phoneNumber);
    }

    @Override
    public int getItemCount() {
        return mPhoneNumbers.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {

        TextView mPhoneNumber;

        public SearchViewHolder(View itemView) {
            super(itemView);
            mPhoneNumber = itemView.findViewById(R.id.phone_number);
        }

        public void bind(String phoneNum) {
            mPhoneNumber.setText(phoneNum);
        }
    }

}
