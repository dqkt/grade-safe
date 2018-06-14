package com.example.dq.gradesafe;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

public class AssignmentListDiffCallback extends DiffUtil.Callback {

    private List<Assignment> oldAssignments;
    private List<Assignment> newAssignments;

    AssignmentListDiffCallback(List<Assignment> oldAssignments, List<Assignment> newAssignments) {
        this.oldAssignments = oldAssignments;
        this.newAssignments = newAssignments;
    }

    @Override
    public int getOldListSize() {
        return oldAssignments.size();
    }

    @Override
    public int getNewListSize() {
        return newAssignments.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldAssignments.get(oldItemPosition).getAssignmentID() == newAssignments.get(newItemPosition).getAssignmentID();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldAssignments.get(oldItemPosition).equals(newAssignments.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

}
