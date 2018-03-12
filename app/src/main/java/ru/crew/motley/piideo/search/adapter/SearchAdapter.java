package ru.crew.motley.piideo.search.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;

import java.util.List;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.search.Country;
import ru.crew.motley.piideo.search.SendRequestCallback;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<Member> mMembers;
    private List<Country> mCountries;
    private SendRequestCallback mListener;

    public SearchAdapter(List<Member> members, List<Country> countries, SendRequestCallback listener) {
        mCountries = countries;
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
        String cc = member.getCountryCode();
        if (cc.equals("8")) {
            cc = "7";
        }
        Country holderCountry = null;
        for (Country country : mCountries) {
            if (country.getCountryCodeStr().equals(cc)) {
                holderCountry = country;
            }
        }
        holder.bind(member, holderCountry);
    }

    @Override
    public int getItemCount() {
        return mMembers.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {

        TextView mPhoneNumber;
        ImageView mFlag;

        public SearchViewHolder(View itemView) {
            super(itemView);
            mPhoneNumber = itemView.findViewById(R.id.phone_number);
            mFlag = itemView.findViewById(R.id.flag_image);
        }

        public void bind(Member member, Country country) {
            mPhoneNumber.setText(country.getCountryName());
            showFlag(country.getFileName());
            String receiverId = member.getChatId();
            itemView.setOnClickListener(v -> {
                startChat(receiverId);
            });
        }

        private void showFlag(String fileName) {
            Context context = itemView.getContext();
            int resId = context.getResources()
                    .getIdentifier(fileName, "drawable", context.getPackageName());
            mFlag.setImageResource(resId);
        }

        private void startChat(String receiverId) {
            mListener.onClick(receiverId, itemView);
        }
    }

}
