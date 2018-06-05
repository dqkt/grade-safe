package com.example.dq.gradesafe;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

public class YearListDiffCallback extends DiffUtil.Callback {

    private List<Year> oldYears;
    private List<Year> newYears;

    public YearListDiffCallback(List<Year> oldYears, List<Year> newYears) {
        this.oldYears = oldYears;
        this.newYears = newYears;
    }

    @Override
    public int getOldListSize() {
        return oldYears.size();
    }

    @Override
    public int getNewListSize() {
        return newYears.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldYears.get(oldItemPosition).getYearID() == newYears.get(newItemPosition).getYearID();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldYears.get(oldItemPosition).equals(newYears.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

}
