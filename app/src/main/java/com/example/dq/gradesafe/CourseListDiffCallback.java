package com.example.dq.gradesafe;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

public class CourseListDiffCallback extends DiffUtil.Callback {

    private List<Course> oldCourses;
    private List<Course> newCourses;

    public CourseListDiffCallback(List<Course> oldCourses, List<Course> newCourses) {
        this.oldCourses = oldCourses;
        this.newCourses = newCourses;
    }

    @Override
    public int getOldListSize() {
        return oldCourses.size();
    }

    @Override
    public int getNewListSize() {
        return newCourses.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldCourses.get(oldItemPosition).getCourseID() == newCourses.get(newItemPosition).getCourseID();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldCourses.get(oldItemPosition).equals(newCourses.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

}
