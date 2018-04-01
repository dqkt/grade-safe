package com.example.dq.gradesafe;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by DQ on 3/31/2018.
 */

public class CourseReactiveRecyclerViewAdapter extends ReactiveRecyclerViewAdapter<CourseViewHolder>
        implements ReactiveRecyclerView.TouchCallback {

    private ArrayList<Course> courses;
    private static DecimalFormat scoreFormatter;

    CourseReactiveRecyclerViewAdapter(Context context, ArrayList<Course> courses, ReactiveRecyclerView reactiveRecyclerView) {
        super(context, reactiveRecyclerView);
        this.courses = courses;
        scoreFormatter = new DecimalFormat("0.0%");

        this.reactiveRecyclerView.setTouchCallback(this);
        this.reactiveRecyclerView.setHeaderClass(CoursesHeader.class);
    }

    @Override
    public boolean isRearranging() {
        return isRearranging;
    }

    @Override
    public boolean hasHeader() {
        return false;
    }

    @Override
    public boolean onMove(ReactiveRecyclerView.ViewHolder viewHolder, ReactiveRecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        if (!(target instanceof CoursesHeader)) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(courses, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(courses, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }
        return false;
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            final ViewPager header = (ViewPager) inflater.inflate(R.layout.layout_slideshow, parent, false);
            return new CoursesHeader(header, context);
        } else if (viewType == TYPE_ITEM) {
            final LinearLayout courseLayout = (LinearLayout) inflater.inflate(R.layout.layout_course, parent, false);
            return new CourseViewHolder(courseLayout, context, reactiveRecyclerView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (!(holder instanceof CoursesHeader)) {
            final Course currentCourse = courses.get(position);

            holder.course = currentCourse;
            holder.name.setText(currentCourse.getName());
            holder.fullName.setText(String.valueOf("Full name of " + currentCourse.getName()));

            int numCredits = currentCourse.getNumCredits();
            if (numCredits == 1) {
                holder.numCredits.setText(String.valueOf(numCredits + " credit"));
            } else {
                holder.numCredits.setText(String.valueOf(numCredits + " credits"));
            }
            holder.overallScore.setText(scoreFormatter.format(currentCourse.getOverallScore()));
            holder.overallGrade.setText(currentCourse.getOverallGrade());
        } else {
            CoursesHeader coursesHeader = (CoursesHeader) holder;
        }
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }
}

class CoursesHeader extends CourseViewHolder {

    public CoursesHeader(View view, Context context) {
        super(view);
    }
}

class CourseViewHolder extends ReactiveRecyclerView.ViewHolder {

    public Context context;

    public Course course;

    public LinearLayout overallLayout;
    public TextView name;
    public TextView fullName;
    public TextView numCredits;
    public TextView overallScore;
    public TextView overallGrade;

    public CourseViewHolder(View view) {
        super(view);
    }

    public CourseViewHolder(View view, Context context, ReactiveRecyclerView reactiveRecyclerView) {
        super(view, reactiveRecyclerView.getAdapter());

        this.context = context;

        overallLayout = (LinearLayout) view;
        name = (TextView) overallLayout.findViewById(R.id.textview_name);
        fullName = (TextView) overallLayout.findViewById(R.id.textview_full_name);
        numCredits = (TextView) overallLayout.findViewById(R.id.textview_num_credits);
        overallScore = (TextView) overallLayout.findViewById(R.id.textview_score);
        overallGrade = (TextView) overallLayout.findViewById(R.id.textview_grade);

        overallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}

