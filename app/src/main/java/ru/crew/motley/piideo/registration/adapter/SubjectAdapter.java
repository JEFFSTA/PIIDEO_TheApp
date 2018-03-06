package ru.crew.motley.piideo.registration.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.network.Subject;
import ru.crew.motley.piideo.registration.SubjectAdapterListener;

/**
 * Created by vas on 2/17/18.
 */

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectHolder> {

    private List<Subject> mSubjects;
    private SubjectAdapterListener mListener;

    public SubjectAdapter(List<Subject> subjects, SubjectAdapterListener listener) {
        mSubjects = subjects;
        mListener = listener;
    }

    @Override
    public SubjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dialog_subject, parent, false);
        return new SubjectHolder(v);
    }

    @Override
    public void onBindViewHolder(SubjectHolder holder, int position) {
        Subject subject = mSubjects.get(position);
        holder.bind(subject);
    }

    public void updateWithUI(List<Subject> subjects) {
        mSubjects.clear();
        mSubjects.addAll(subjects);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mSubjects.size();
    }

    class SubjectHolder extends RecyclerView.ViewHolder {

        private TextView mSubjectName;

        public SubjectHolder(View itemView) {
            super(itemView);
            mSubjectName = itemView.findViewById(R.id.subject_name);
        }

        public void bind(Subject subject) {
            String subjectName = subject.getName()
                    .replaceAll("^.", subject.getName().substring(0,1).toUpperCase());
            mSubjectName.setText(subjectName);
            itemView.setOnClickListener(v -> mListener.onSubjectSelected(subject));
        }
    }
}
