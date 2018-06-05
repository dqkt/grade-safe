package com.example.dq.gradesafe;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

public class TermListDiffCallback extends DiffUtil.Callback {

    private List<Term> oldTerms;
    private List<Term> newTerms;

    public TermListDiffCallback(List<Term> oldTerms, List<Term> newTerms) {
        this.oldTerms = oldTerms;
        this.newTerms = newTerms;
    }

    @Override
    public int getOldListSize() {
        return oldTerms.size();
    }

    @Override
    public int getNewListSize() {
        return newTerms.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldTerms.get(oldItemPosition).getTermID() == newTerms.get(newItemPosition).getTermID();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldTerms.get(oldItemPosition).equals(newTerms.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

}
