package ru.crew.motley.piideo.search.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.crew.motley.piideo.R;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    List<String> mSubjects;
    SubjectListener mSubjectListener;

    public interface SubjectListener {
        void onClick(String subject);
    }

    public SubjectAdapter(List<String> subjects, SubjectListener callback) {
        mSubjects = subjects;
        mSubjectListener = callback;
    }

    @Override
    public SubjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SubjectViewHolder holder, int position) {
        String subject = mSubjects.get(position);
        holder.bind(subject);
    }

    @Override
    public int getItemCount() {
        return mSubjects.size();
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder {

        TextView mSubject;

        public SubjectViewHolder(View itemView) {
            super(itemView);
            mSubject = itemView.findViewById(R.id.subjectName);
        }

        public void bind(String subject) {
            mSubject.setText(subject);
            itemView.setOnClickListener(l -> mSubjectListener.onClick(subject));
        }
    }
}
