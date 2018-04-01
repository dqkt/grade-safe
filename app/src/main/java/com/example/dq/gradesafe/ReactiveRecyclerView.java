package com.example.dq.gradesafe;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by DQ on 3/21/2018.
 */

public class ReactiveRecyclerView extends RecyclerView {

    private Context context;
    private SwipeUtil swipeUtil;
    private TouchCallback touchCallback;

    private int dragDirs;
    private int swipeDirs;

    private Class<?> headerClass;

    public ReactiveRecyclerView(Context context) {
        super(context);
        this.context = context;
        setUpSwipeUtility();
    }

    public ReactiveRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ReactiveRecyclerView,0, 0);

        try {
            dragDirs = a.getInteger(R.styleable.ReactiveRecyclerView_dragDirs, 0);
            swipeDirs = a.getInteger(R.styleable.ReactiveRecyclerView_swipeDirs, 0);
        } finally {
            a.recycle();
        }
        setUpSwipeUtility();
    }

    public ReactiveRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ReactiveRecyclerView,0, 0);

        try {
            dragDirs = a.getInteger(R.styleable.ReactiveRecyclerView_dragDirs, 0);
            swipeDirs = a.getInteger(R.styleable.ReactiveRecyclerView_swipeDirs, 0);
        } finally {
            a.recycle();
        }
        setUpSwipeUtility();
    }

    public void setTouchCallback(TouchCallback touchCallback) {
        this.touchCallback = touchCallback;
    }

    public interface TouchCallback {
        boolean onMove(ReactiveRecyclerView.ViewHolder viewHolder, ReactiveRecyclerView.ViewHolder target);
        void setIsRearranging(boolean isRearranging);
    }

    public Class<?> getHeaderClass() {
        return headerClass;
    }
    public void setHeaderClass(Class<?> headerClass) {
        this.headerClass = headerClass;
    }

    private void setUpSwipeUtility() {
        this.touchCallback = null;
        if (this.swipeUtil == null) {
            this.swipeUtil = new SwipeUtil(0, 0, context) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    if (touchCallback != null) {
                        if (viewHolder instanceof ReactiveRecyclerView.ViewHolder && target instanceof ReactiveRecyclerView.ViewHolder) {
                            return touchCallback.onMove((ReactiveRecyclerView.ViewHolder) viewHolder, (ReactiveRecyclerView.ViewHolder) target);
                        }
                    }
                    return false;
                }

                @Override
                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);

                    int numItems = recyclerView.getChildCount();
                    View yearView;
                    for (int i = 0; i < numItems; i++) {
                        yearView = recyclerView.getChildAt(i);
                        ((ReactiveRecyclerView.ViewHolder) recyclerView.getChildViewHolder(yearView)).setIsLongClicked(false);
                        yearView.animate().alpha(1.0f).setListener(null);
                    }
                    touchCallback.setIsRearranging(false);
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                }

                @Override
                public boolean isItemViewSwipeEnabled() {
                    return true;
                }

                @Override
                public boolean isLongPressDragEnabled() {
                    return true;
                }

                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    if (viewHolder.getClass().equals(headerClass)) {
                        return makeMovementFlags(0, 0);
                    } else {
                        return makeMovementFlags(dragDirs, swipeDirs);
                    }
                }

                @Override
                public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }
            };
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeUtil);
            itemTouchHelper.attachToRecyclerView(this);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View overallLayout;
        private boolean isLongClicked;

        ViewHolder(final View view) {
            super(view);
            this.overallLayout = view;
            this.isLongClicked = false;
        }

        ViewHolder(final View view, final ReactiveRecyclerView.Adapter adapter) {
            super(view);
            this.overallLayout = view;
            this.isLongClicked = false;

            if (adapter instanceof ReactiveRecyclerViewAdapter) {
                final ReactiveRecyclerViewAdapter reactiveRecyclerViewAdapter = (ReactiveRecyclerViewAdapter) adapter;
                view.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        isLongClicked = true;
                        reactiveRecyclerViewAdapter.setIsRearranging(true);
                        return false;
                    }
                });

                view.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        if (action == MotionEvent.ACTION_UP && isLongClicked || (action == MotionEvent.ACTION_CANCEL && !reactiveRecyclerViewAdapter.isRearranging())) {
                            isLongClicked = false;
                            reactiveRecyclerViewAdapter.setIsRearranging(false);
                            if (action == MotionEvent.ACTION_UP) {
                                v.performClick();
                            }
                        }
                        return false;
                    }
                });
            }
        }

        boolean isLongClicked() {
            return isLongClicked;
        }
        
        void setIsLongClicked(boolean isLongClicked) {
            this.isLongClicked = isLongClicked;
        }
    }
}
