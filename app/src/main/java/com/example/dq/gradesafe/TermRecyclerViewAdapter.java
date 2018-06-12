package com.example.dq.gradesafe;

import android.app.Activity;
import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by DQ on 3/19/2018.
 */

public class TermRecyclerViewAdapter extends RecyclerView.Adapter<TermViewHolder> {

    static final int TYPE_HEADER = 0;
    static final int TYPE_ITEM = 1;

    private Context context;
    private LayoutInflater inflater;

    private List<Term> terms;
    private DecimalFormat creditsFormatter;
    private DecimalFormat gpaFormatter;
    private TermListViewModel termListViewModel;

    private Year year;

    protected boolean isBinding;

    TermRecyclerViewAdapter(Context context, List<Term> terms, TermListViewModel termListViewModel, Year year) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);

        this.terms = terms;
        this.creditsFormatter = new DecimalFormat("#.#");
        this.gpaFormatter = new DecimalFormat("0.000");
        this.termListViewModel = termListViewModel;

        this.year = year;
    }

    /*@Override
    public boolean hasHeader() {
        return false;
    }*/

    public void onMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        Term fromTerm, toTerm;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                fromTerm = terms.get(i);
                toTerm = terms.get(i + 1);
                fromTerm.setListIndex(i + 1);
                toTerm.setListIndex(i);
                termListViewModel.updateTerm(fromTerm);
                termListViewModel.updateTerm(toTerm);
                Collections.swap(terms, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                fromTerm = terms.get(i);
                toTerm = terms.get(i - 1);
                fromTerm.setListIndex(i - 1);
                toTerm.setListIndex(i);
                termListViewModel.updateTerm(fromTerm);
                termListViewModel.updateTerm(toTerm);
                Collections.swap(terms, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public void updateTerms(List<Term> terms) {
        DiffUtil.DiffResult difference = DiffUtil.calculateDiff(new TermListDiffCallback(this.terms, terms));
        this.terms.clear();
        this.terms.addAll(terms);
        difference.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    @Override
    public TermViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LinearLayout termLayout = (LinearLayout) inflater.inflate(R.layout.layout_term, parent, false);
        return new TermViewHolder(termLayout, context, year);
    }

    @Override
    public void onBindViewHolder(TermViewHolder holder, int position) {
        isBinding = true;
        // super.onBindViewHolder(holder, position);
        final Term currentTerm = terms.get(position);

        holder.term = currentTerm;

        holder.name.setText(currentTerm.getName());

        double totalNumCredits = currentTerm.getTotalNumCredits();
        if (totalNumCredits == 1) {
            holder.totalNumCredits.setText(String.valueOf("1 credit"));
        } else {
            holder.totalNumCredits.setText(String.valueOf(creditsFormatter.format(totalNumCredits) + " credits"));
        }

        if (totalNumCredits == 0) {
            holder.termGPA.setText("");
        } else {
            holder.termGPA.setText(gpaFormatter.format(currentTerm.getGpa()));
        }
        isBinding = false;
    }

    @Override
    public int getItemCount() {
        return terms.size();
    }
}

class TermViewHolder extends RecyclerView.ViewHolder {

    private final Context context;

    public Term term;
    private Year year;

    public View overallLayout;
    public TextView name;
    public TextView totalNumCredits;
    public TextView termGPA;

    TermViewHolder(View view, final Context context, Year year) {
        super(view);

        this.context = context;

        this.year = year;

        overallLayout = view;
        name = (TextView) view.findViewById(R.id.textview_name);
        totalNumCredits = (TextView) view.findViewById(R.id.textview_num_credits);
        termGPA = (TextView) view.findViewById(R.id.textview_gpa);

        overallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, TermActivity.class);
                i.putExtra(TermActivity.SELECTED_TERM_KEY, term);
                i.putExtra(TermActivity.CORRESPONDING_YEAR_KEY, TermViewHolder.this.year);
                /*ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                        Pair.create((View) overallLayout, overallLayout.getTransitionName()));
                context.startActivity(i, options.toBundle());*/
                context.startActivity(i);
            }
        });
    }
}