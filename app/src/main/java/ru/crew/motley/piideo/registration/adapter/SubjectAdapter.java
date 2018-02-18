package ru.crew.motley.piideo.registration.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.network.Subject;

/**
 * Created by vas on 2/17/18.
 */

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectHolder> {

    private List<Subject> mSubjects;

    public SubjectAdapter(List<Subject> subjects) {
        mSubjects = subjects;
    }

    @Override
    public SubjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        return new SubjectHolder(v);
    }

    @Override
    public void onBindViewHolder(SubjectHolder holder, int position) {
        Subject subject = mSubjects.get(position);
        holder.bind(subject);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class SubjectHolder extends RecyclerView.ViewHolder {

        private TextView mSubjectName;

        public SubjectHolder(View itemView) {
            super(itemView);
            mSubjectName = itemView.findViewById(android.R.id.text1);
        }

        public void bind(Subject subject) {
            mSubjectName.setText(subject.getName());
        }
    }
}
