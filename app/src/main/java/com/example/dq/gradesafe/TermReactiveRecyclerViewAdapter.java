package com.example.dq.gradesafe;

import android.content.Context;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by DQ on 3/19/2018.
 */

public class TermReactiveRecyclerViewAdapter extends ReactiveRecyclerViewAdapter<TermViewHolder> implements ReactiveRecyclerView.TouchCallback {

    private ArrayList<Term> terms;

    public TermReactiveRecyclerViewAdapter(Context context, ArrayList<Term> terms, ReactiveRecyclerView reactiveRecyclerView) {
        super(context, reactiveRecyclerView);
        this.terms = terms;
        this.reactiveRecyclerView.setTouchCallback(this);
    }

    @Override
    public boolean hasHeader() {
        return false;
    }

    @Override
    public boolean onMove(ReactiveRecyclerView.ViewHolder viewHolder, ReactiveRecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(terms, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(terms, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    @Override
    public TermViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LinearLayout termLayout = (LinearLayout) inflater.inflate(R.layout.layout_term, parent, false);
        return new TermViewHolder(termLayout, context, reactiveRecyclerView);
    }

    @Override
    public void onBindViewHolder(TermViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final Term currentTerm = terms.get(position);
        currentTerm.updateTotalNumCredits();
        currentTerm.updateGPA();

        holder.overallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, TermActivity.class);
                i.putExtra(TermActivity.SELECTED_TERM_KEY, currentTerm);
                context.startActivity(i);
            }
        });

        holder.name.setText(currentTerm.getName());

        int totalNumCredits = currentTerm.getTotalNumCredits();
        if (totalNumCredits == 1) {
            holder.totalNumCredits.setText(String.valueOf(totalNumCredits + " credit"));
        } else {
            holder.totalNumCredits.setText(String.valueOf(totalNumCredits + " credits"));
        }

        if (totalNumCredits == 0) {
            holder.termGPA.setText("");
        } else {
            holder.termGPA.setText(String.valueOf(currentTerm.getGPA()));
        }
    }

    @Override
    public int getItemCount() {
        return terms.size();
    }
}

class TermViewHolder extends ReactiveRecyclerView.ViewHolder {

    private final Context context;

    public View overallLayout;
    public TextView name;
    public TextView totalNumCredits;
    public TextView termGPA;

    public TermViewHolder(View view, final Context context, ReactiveRecyclerView recyclerView) {
        super(view, recyclerView.getAdapter());

        this.context = context;

        overallLayout = view;
        name = (TextView) view.findViewById(R.id.textview_name);
        totalNumCredits = (TextView) view.findViewById(R.id.textview_num_credits);
        termGPA = (TextView) view.findViewById(R.id.textview_gpa);
    }
}