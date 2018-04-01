package com.example.dq.gradesafe;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by DQ on 3/20/2018.
 */

public class CustomScroller extends Scroller {

    private int duration;

    public CustomScroller(Context context, Interpolator interpolator, int duration) {
        super(context, interpolator);
        this.duration = duration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, this.duration);
    }
}