package com.example.dq.gradesafe;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by DQ on 3/21/2018.
 */

public abstract class ReactiveRecyclerViewAdapter<ViewHolder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<ViewHolder> {

    static final int TYPE_HEADER = 0;
    static final int TYPE_ITEM = 1;

    protected Context context;
    protected LayoutInflater inflater;

    protected ReactiveRecyclerView reactiveRecyclerView;
    protected boolean isRearranging;

    public ReactiveRecyclerViewAdapter(Context context, ReactiveRecyclerView reactiveRecyclerView) {
        this.context = context;
        this.reactiveRecyclerView = reactiveRecyclerView;
        this.inflater = LayoutInflater.from(context);
        this.isRearranging = false;
    }

    public boolean isRearranging() {
        return isRearranging;
    }

    public void setIsRearranging(boolean isRearranging) {
        this.isRearranging = isRearranging;
        LinearLayoutManager layoutManager = (LinearLayoutManager) reactiveRecyclerView.getLayoutManager();
        int firstVisible = layoutManager.findFirstVisibleItemPosition();
        int lastVisible = layoutManager.findLastVisibleItemPosition();
        View view;
        if (isRearranging) {
            for (int i = 0; i < reactiveRecyclerView.getChildCount(); i++) {
                view = reactiveRecyclerView.getChildAt(i);
                if (!((reactiveRecyclerView.getChildViewHolder(view)).getClass().equals(reactiveRecyclerView.getHeaderClass()))
                        && !((ReactiveRecyclerView.ViewHolder) reactiveRecyclerView.getChildViewHolder(view)).isLongClicked()) {
                    view.animate().alpha(0.5f).setListener(null);
                }
            }
        } else {
            for (int i = 0; i < reactiveRecyclerView.getChildCount(); i++) {
                view = reactiveRecyclerView.getChildAt(i);
                view.animate().alpha(1.0f).setListener(null);
            }
        }
        notifyItemRangeChanged(0, firstVisible, new ArrayList<>());
        notifyItemRangeChanged(lastVisible + 1, getItemCount() - lastVisible, new ArrayList<>());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return (hasHeader() && position == 0) ? TYPE_HEADER : TYPE_ITEM;
    }

    public abstract boolean hasHeader();

    @Override
    public abstract ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    @CallSuper
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof ReactiveRecyclerView.ViewHolder) {
            if (getItemViewType(position) != ReactiveRecyclerViewAdapter.TYPE_HEADER) {
                if (isRearranging() && !((ReactiveRecyclerView.ViewHolder) holder).isLongClicked()) {
                    ((ReactiveRecyclerView.ViewHolder) holder).overallLayout.animate().alpha(0.5f).setListener(null);
                } else {
                    ((ReactiveRecyclerView.ViewHolder) holder).overallLayout.animate().alpha(1.0f).setListener(null);
                }

                if (isRearranging) {
                    ((ReactiveRecyclerView.ViewHolder) holder).overallLayout.setAlpha(0.5f);
                } else {
                    ((ReactiveRecyclerView.ViewHolder) holder).overallLayout.setAlpha(1.0f);
                }
            }
        }
    }

    @Override
    public abstract int getItemCount();
}